package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessDefinitionTemplate;
import com.magus.framework.repository.BaseRepository;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessDefinitionTemplate原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessDefinitionTemplateRepository extends BaseRepository<ProcessDefinitionTemplate, String> {
    ProcessDefinitionTemplate findByProcDefIdAndDeletedIsFalse(String procDefId);
}
