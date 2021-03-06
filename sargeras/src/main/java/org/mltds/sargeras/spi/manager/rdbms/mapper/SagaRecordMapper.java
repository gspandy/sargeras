package org.mltds.sargeras.spi.manager.rdbms.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.api.model.SagaRecord;

/**
 * @author sunyi
 */
public interface SagaRecordMapper {

    int insert(SagaRecord sagaRecord);

    void updateStatus(@Param("id") long id, @Param("status") SagaStatus status, @Param("modifyTime") Date modifyTime);

    int updateForLock(@Param("id") long id, @Param("oldTriggerId") String oldTriggerId, @Param("newTriggerId") String newTriggerId,
            @Param("lockExpireTime") Date lockExpireTime);

    int updateForUnlock(@Param("id") long id, @Param("oldTriggerId") String oldTriggerId);

    int updateNextTriggerTimeAndIncrementCount(@Param("id") long id, @Param("nextTriggerTime") Date nextTriggerTime, @Param("modifyTime") Date modifyTime);

    SagaRecord selectById(long id);

    SagaRecord selectByBiz(@Param("appName") String appName, @Param("bizName") String bizName, @Param("bizId") String bizId);

    List<Long> selectNeedRetryRecordList(@Param("beforeTriggerTime") Date beforeTriggerTime, @Param("limit") int limit);

}