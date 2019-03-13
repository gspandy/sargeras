package org.mltds.sargeras.repository.rdbms.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.repository.rdbms.model.ContextDO;

/**
 * @author sunyi
 */
public interface ContextMapper {

    ContextDO selectById(Long id);

    ContextDO selectByBiz(String appName, String bizName, String bizId);

    int insert(ContextDO context);

    /**
     * 仅更新有限的字段(status,pre_executed_tx，pre_compensated_tx,modify_time)通用方法
     */
    int updateById(ContextDO context);

    int incrementTriggerCount(@Param("id") Long id, @Param("modifyTime") Date modifyTime);

    List<Long> findNeedRetryContextList(@Param("triggerTime") Date triggerTime, @Param("limit") int limit);
}
