package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessInstanceAppendixRt;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/6 3:10 PM
 */
public interface ProcessInstanceAppendixRtRepository extends BaseRepository<ProcessInstanceAppendixRt, String> {
    List<ProcessInstanceAppendixRt> findByLocalProcInstIdAndDeletedIsFalse(String procInstId);
}
