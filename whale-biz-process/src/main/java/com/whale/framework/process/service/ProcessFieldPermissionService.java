package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.rsp.ProcessFieldPermissionRsp;
import com.whale.framework.process.entity.ProcessFieldPermission;
import com.whale.framework.process.repository.ProcessFieldPermissionRepository;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程定义实现
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Slf4j
@Service
@GlobalTransactional
public class ProcessFieldPermissionService extends BaseService<ProcessFieldPermission, String> {

    @Autowired
    ProcessFieldPermissionRepository repository;

    /**
     * 查询节点的字段权限
     *
     * @param procDefId
     * @param taskDefKey
     * @return
     */
    public List<ProcessFieldPermissionRsp> findFieldPermission(String procDefId, String taskDefKey) {
        if (StringUtils.isBlank(procDefId)) {
            return new ArrayList<>();
        }
        List<ProcessFieldPermission> fieldsPermission = new ArrayList<>();
        if (StringUtils.isBlank(taskDefKey)) {
            fieldsPermission = repository.findByProcDefIdAndDeletedIsFalse(procDefId);
        } else {
            fieldsPermission = repository.findByProcDefIdAndNodeKeyAndDeletedIsFalse(procDefId, taskDefKey);
        }
        return MagusUtils.copyList(fieldsPermission, ProcessFieldPermissionRsp.class);
    }
}
