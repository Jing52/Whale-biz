package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessDelegate;
import com.magus.framework.repository.BaseRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2022/12/9 4:32 PM
 */
public interface ProcessDelegateRepository extends BaseRepository<ProcessDelegate, String> {
    List<ProcessDelegate> findByBatchNoAndDeletedIsFalse(String batchNo);

    List<ProcessDelegate> findByBatchNoAndStateIsTrueAndDeletedIsFalse(String batchNo);

    List<ProcessDelegate> findByBatchNoInAndDeletedIsFalse(List<String> batchNos);

    List<ProcessDelegate> findByEndTimeLessThanAndStateAndDeletedIsFalse(Date date, Integer state);

    List<ProcessDelegate> findBySysGroupIdInAndCreateIdAndStateIsTrueAndDeletedIsFalse(List<String> sysGroupIds, String createId);
}
