package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessCCInstance;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2022/12/9 4:14 PM
 */
public interface ProcessCCInstanceRepository extends BaseRepository<ProcessCCInstance, String> {
    List<ProcessCCInstance> findByProcInstIdAndStateAndDeletedIsFalse(String procInstId, int state);

    List<ProcessCCInstance> findByProcInstIdAndUserIdAndStateAndDeletedIsFalseOrderByCreateTimeDesc(String procInstId, String userId, int state);

    ProcessCCInstance findTop1ByProcInstIdAndUserIdAndStateAndDeletedIsFalseOrderByCreateTimeDesc(String procInstId, String loginId, int state);
}
