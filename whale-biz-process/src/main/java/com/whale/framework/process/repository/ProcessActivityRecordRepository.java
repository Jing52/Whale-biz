package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessActivityRecord;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2022/12/16 9:24 AM
 */
public interface ProcessActivityRecordRepository extends BaseRepository<ProcessActivityRecord, String> {
    List<ProcessActivityRecord> findByProcInstIdAndDeletedIsFalseOrderBySortAsc(String procInstId);

    ProcessActivityRecord findTop1ByProcInstIdOrderBySortDesc(String procInstId);
}
