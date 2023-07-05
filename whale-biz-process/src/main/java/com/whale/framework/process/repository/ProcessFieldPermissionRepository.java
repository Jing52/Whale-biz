package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessFieldPermission;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessDefinition原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessFieldPermissionRepository extends BaseRepository<ProcessFieldPermission, String> {
    List<ProcessFieldPermission> findByProcDefIdAndDeletedIsFalse(String procDefId);

    List<ProcessFieldPermission> findByProcDefIdAndNodeKeyAndDeletedIsFalse(String procDefId, String taskDefKey);
}
