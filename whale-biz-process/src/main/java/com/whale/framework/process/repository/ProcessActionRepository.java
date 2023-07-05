package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessAction;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2023/5/4 10:35 AM
 */
public interface ProcessActionRepository extends BaseRepository<ProcessAction, String> {
    List<ProcessAction> findByCodeInAndDeletedIsFalse(List<String> codes);
}
