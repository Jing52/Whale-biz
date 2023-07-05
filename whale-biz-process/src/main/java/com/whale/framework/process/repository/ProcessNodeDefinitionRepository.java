package com.whale.framework.process.repository;

import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.entity.ProcessNode;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description: ProcessActDefinition原子层
 * @Author: Whale
 * @Date: 2022/12/5 3:30 PM
 */
public interface ProcessNodeDefinitionRepository extends BaseRepository<ProcessNode, String> {

    List<ProcessNode> findByProcDefIdAndDeletedIsFalse(String procDefId);

    ProcessNode findByProcDefIdAndNodeKeyAndDeletedIsFalse(String procDefId, String nodeKey);

    List<ProcessNode> findByApplicantIsTrueAndDeletedIsFalse();

    List<ProcessNode> findByProcDefIdAndNodeKeyInAndDeletedIsFalse(String procDefId, List<String> nodeKeys);

    List<ProcessNode> findByNodeKeyInAndApplicantIsTrueAndDeletedIsFalse(List<String> nodeKeys);
}
