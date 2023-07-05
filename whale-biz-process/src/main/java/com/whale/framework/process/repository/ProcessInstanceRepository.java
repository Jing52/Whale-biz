package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessInstance;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessInstance原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessInstanceRepository extends BaseRepository<ProcessInstance, String> {
    List<ProcessInstance> findByProcInstIdInAndDeletedIsFalse(List<String> procInstIds);

    ProcessInstance findByProcInstIdAndDeletedIsFalse(String procInstId);

    List<ProcessInstance> findByBusinessIdInAndDeletedIsFalse(List<String> dataId);

    ProcessInstance findByBusinessIdAndDeletedIsFalse(String dataId);

    List<ProcessInstance> findByProcDefIdInAndDeletedIsFalse(List<String> procDefIds);

    List<ProcessInstance> findByProcDefIdAndStatusAndDeletedIsFalse(String procDefId, String status);

    ProcessInstance findByBusinessTableNameAndBusinessIdAndDeletedIsFalse(String businessTableName, String businessId);

    ProcessInstance findByIdAndStatusAndDeletedIsFalse(String id, String code);

}
