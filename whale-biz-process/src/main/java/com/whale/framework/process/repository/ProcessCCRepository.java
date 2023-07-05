package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessCC;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2022/12/9 4:14 PM
 */
public interface ProcessCCRepository extends BaseRepository<ProcessCC, String> {
    List<ProcessCC> findByProcDefIdAndNodeKeyAndDeletedIsFalse(String procDefId, String nodeKey);
}
