package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessTask;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2023/5/10 5:42 PM
 */
public interface ProcessTaskRepository extends BaseRepository<ProcessTask, String> {
    ProcessTask findByTaskIdAndDeletedIsFalse(String taskId);

    List<ProcessTask> findByProcInstIdAndDeletedIsFalse(String procInstId);

    List<ProcessTask> findByTaskIdInAndDeletedIsFalse(List<String> taskIds);

    List<ProcessTask> findByProcInstIdAndNodeKeyAndDeletedIsFalseOrderByCreateTime(String procInstId, String nodeKey);
}
