package org.mltds.sargeras.repository.rdbms;

import java.util.Calendar;
import java.util.Date;

import org.mltds.sargeras.api.*;
import org.mltds.sargeras.exception.SagaException;
import org.mltds.sargeras.repository.Repository;
import org.mltds.sargeras.repository.rdbms.mapper.ContextInfoMapper;
import org.mltds.sargeras.repository.rdbms.mapper.ContextLockMapper;
import org.mltds.sargeras.repository.rdbms.mapper.ContextMapper;
import org.mltds.sargeras.repository.rdbms.model.ContextDO;
import org.mltds.sargeras.repository.rdbms.model.ContextInfoDO;
import org.mltds.sargeras.repository.rdbms.model.ContextLockDO;
import org.mltds.sargeras.serialize.Serialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi
 */
public class RdbmsRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsRepository.class);

    private ContextMapper contextMapper;
    private ContextInfoMapper contextInfoMapper;
    private ContextLockMapper contextLockMapper;

    private Serialize serialize = SagaApplication.getSerialize();

    @Override
    public Long saveContext(SagaContext context) {

        ContextDO contextDO = sagaContextToContextDO(context);
        int insert = contextMapper.insert(contextDO);
        context.setId(contextDO.getId());
        return contextDO.getId();
    }

    @Override
    public SagaContext loadContext(Long id) {

        ContextDO contextDO = contextMapper.selectById(id);
        if (contextDO == null) {
            throw new SagaException("查找Context失败，ID: " + id);
        }

        String appName = contextDO.getAppName();
        String bizName = contextDO.getBizName();

        Saga saga = SagaApplication.getSaga(appName, bizName);
        if (saga == null) {
            throw new SagaException("查询 Saga 失败，AppName：" + appName + ", BizName: " + bizName);
        }

        try {
            SagaContext sagaContext = contextDOToSagaContext(contextDO, saga);
            return sagaContext;
        } catch (ClassNotFoundException e) {
            throw new SagaException("重新构建SagaContext失败，ID：" + id, e);
        }

    }

    @Override
    public void saveContextStatus(Long contextId, SagaStatus status) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setStatus(status);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void saveCurrentTx(Long contextId, Class<? extends SagaTx> cls) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setCurrentTx(cls.getName());
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void savePreExecutedTx(Long contextId, Class<? extends SagaTx> cls) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setPreExecutedTx(cls.getName());
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void savePreCompensatedTx(Long contextId, Class<? extends SagaTx> cls) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setPreCompensatedTx(cls.getName());
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void saveContextInfo(Long contextId, String key, Object info) {
        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null) {
            contextInfoDO = new ContextInfoDO();
            contextInfoDO.setContextId(contextId);
            contextInfoDO.setKey(key);
            String infoStr = serialize.write(info);
            contextInfoDO.setInfo(infoStr);
            Date now = new Date();
            contextInfoDO.setCreateTime(now);
            contextInfoDO.setModifyTime(now);

            contextInfoMapper.insert(contextInfoDO);
        } else {

            String infoStr = serialize.write(info);
            contextInfoDO.setKey(infoStr);
            contextInfoDO.setModifyTime(new Date());

            contextInfoMapper.updateById(contextInfoDO);
        }
    }

    @Override
    public <T> T loadContextInfo(Long contextId, String key, Class<T> cls) {

        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null || contextInfoDO.getInfo() == null) {
            return null;
        }
        return serialize.read(contextInfoDO.getInfo(), cls);
    }

    @Override
    public boolean lock(Long id, String reqId, int timeoutSec) {

        try {
            ContextLockDO contextLockDO = contextLockMapper.select(id);
            if (contextLockDO == null) {
                contextLockDO = newLock(id, reqId, timeoutSec);
                contextLockMapper.insert(contextLockDO);
                return true;
            } else {
                Calendar c = Calendar.getInstance();
                boolean after = c.getTime().after(contextLockDO.getExpireTime());
                if (after) {
                    int delete = contextLockMapper.delete(id, reqId);
                    if (delete <= 0) {
                        return false;
                    } else {
                        contextLockDO = newLock(id, reqId, timeoutSec);
                        contextLockMapper.insert(contextLockDO);
                    }
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn("操作数据库获取锁失败ContextId:{},ReqId:{}", new Object[] { id, reqId }, e);
            return false;
        }
    }

    private ContextLockDO newLock(Long id, String reqId, int timeoutSec) {
        ContextLockDO contextLockDO = new ContextLockDO();
        contextLockDO.setContextId(id);
        contextLockDO.setReqId(reqId);

        Calendar c = Calendar.getInstance();
        contextLockDO.setCreateTime(c.getTime());

        c.add(Calendar.SECOND, timeoutSec);
        contextLockDO.setExpireTime(c.getTime());

        return contextLockDO;
    }

    @Override
    public boolean unlock(Long id, String reqId) {

        try {
            ContextLockDO contextLockDO = contextLockMapper.select(id);
            if (contextLockDO == null) {
                return false;
            } else {
                if (contextLockDO.getReqId().equals(reqId)) {
                    int delete = contextLockMapper.delete(id, reqId);
                    return delete > 0;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("操作数据库释放锁失败ContextId:{},ReqId:{}", new Object[] { id, reqId }, e);
            return false;
        }
    }

    private ContextDO sagaContextToContextDO(SagaContext sagaContext) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(sagaContext.getId());
        contextDO.setAppName(sagaContext.getSaga().getAppName());
        contextDO.setBizName(sagaContext.getSaga().getBizName());
        contextDO.setBizId(sagaContext.getBizId());
        contextDO.setStatus(sagaContext.getStatus());

        if (sagaContext.getCurrentTx() != null) {
            contextDO.setCurrentTx(sagaContext.getCurrentTx().getName());
        }

        if (sagaContext.getPreExecutedTx() != null) {
            contextDO.setPreExecutedTx(sagaContext.getPreExecutedTx().getName());
        }
        if (sagaContext.getPreCompensatedTx() != null) {
            contextDO.setPreCompensatedTx(sagaContext.getPreCompensatedTx().getName());
        }

        contextDO.setCreateTime(new Date());
        contextDO.setModifyTime(new Date());
        return contextDO;
    }

    @SuppressWarnings("unchecked")
    private SagaContext contextDOToSagaContext(ContextDO contextDO, Saga saga) throws ClassNotFoundException {

        SagaContext sagaContext = new SagaContext(saga);
        sagaContext.setId(contextDO.getId());
        sagaContext.setBizId(contextDO.getBizId());
        sagaContext.setStatus(contextDO.getStatus());

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Class<?> preExecutedTx = Class.forName(contextDO.getPreExecutedTx(), false, classLoader);
        sagaContext.setPreExecutedTx((Class<? extends SagaTx>) preExecutedTx);

        Class<?> preCompensatedTx = Class.forName(contextDO.getPreCompensatedTx(), false, classLoader);
        sagaContext.setPreCompensatedTx((Class<? extends SagaTx>) preCompensatedTx);

        return sagaContext;
    }

    void setContextMapper(ContextMapper contextMapper) {
        this.contextMapper = contextMapper;
    }

    void setContextInfoMapper(ContextInfoMapper contextInfoMapper) {
        this.contextInfoMapper = contextInfoMapper;
    }

    void setContextLockMapper(ContextLockMapper contextLockMapper) {
        this.contextLockMapper = contextLockMapper;
    }

}