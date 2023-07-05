package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessSchedule;
import com.magus.framework.repository.BaseRepository;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessDefinition原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessScheduleRepository extends BaseRepository<ProcessSchedule, String> {
    ProcessSchedule findByProcDefIdAndNodeKeyAndDeletedIsFalse(String processDefinitionId, String nodeKey);
}
