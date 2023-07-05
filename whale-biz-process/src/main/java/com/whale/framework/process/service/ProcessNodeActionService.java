package com.whale.framework.process.service;

import com.whale.framework.process.entity.ProcessNodeAction;
import com.whale.framework.process.repository.ProcessNodeActionRepository;
import com.magus.framework.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2023/5/4 10:31 AM
 */
@Service
@Slf4j
public class ProcessNodeActionService extends BaseService<ProcessNodeAction, String> {

    @Autowired
    ProcessNodeActionRepository repository;

    /**
     * 获取操作权限
     *
     * @param nodeKey
     * @return
     */
    public List<ProcessNodeAction> findByNodeKey(String nodeKey) {
        if (StringUtils.isBlank(nodeKey)) {
            return new ArrayList<>();
        }
        List<ProcessNodeAction> nodeActions = repository.findByNodeKeyAndDeletedIsFalse(nodeKey);
        return nodeActions;
    }

    public List<ProcessNodeAction> listByNodeKey(List<String> nodeKeys) {
        if (CollectionUtils.isEmpty(nodeKeys)) {
            return new ArrayList<>();
        }
        return repository.findByNodeKeyInAndDeletedIsFalse(nodeKeys);
    }
}
