package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessDefinition;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessDefinition原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessDefinitionRepository extends BaseRepository<ProcessDefinition, String> {
    ProcessDefinition findByProcDefIdAndDeletedIsFalse(String procDefId);

    List<ProcessDefinition> findByProcDefKeyInAndStateIsTrueAndDeletedIsFalse(List<String> procDefKeys);

    List<ProcessDefinition> findByProcDefIdInAndDeletedIsFalse(List<String> procDefIds);

    ProcessDefinition findByProcDefKeyAndStateIsTrueAndDeletedIsFalse(String procDefKey);

    List<ProcessDefinition> findByProcDefIdInAndStateIsTrueAndDeletedIsFalse(List<String> procDefIds);

    List<ProcessDefinition> findByProcDefKeyAndDeletedIsFalse(String procDefKey);

    List<ProcessDefinition> findByProcDefKeyInAndDeletedIsFalse(List<String> procDefKeys);
}
