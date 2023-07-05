package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessNodeAction;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2023/5/4 10:35 AM
 */
public interface ProcessNodeActionRepository extends BaseRepository<ProcessNodeAction, String> {
    List<ProcessNodeAction> findByNodeKeyAndDeletedIsFalse(String nodeKey);

    List<ProcessNodeAction> findByNodeKeyInAndDeletedIsFalse(List<String> nodeKeys);
}
