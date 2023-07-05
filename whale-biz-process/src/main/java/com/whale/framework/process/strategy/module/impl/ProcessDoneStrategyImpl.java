package com.whale.framework.process.strategy.module.impl;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.whale.framework.process.dto.ProcessHistoricInstanceDTO;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.entity.ProcessNode;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.service.ProcessInstanceService;
import com.whale.framework.process.service.ProcessNodeService;
import com.whale.framework.process.strategy.module.IProcessStrategy;
import com.whale.framework.process.templates.BaseProcessTodoOrDoneTemplate;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.impl
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
@Slf4j
public class ProcessDoneStrategyImpl extends BaseProcessTodoOrDoneTemplate implements IProcessStrategy {

    @Autowired
    HistoryService historyService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessNodeService processNodeService;

    @Override
    public Integer count() {
        log.info("=========================> 已办 所有的任务计数 开始");
        List<ProcessHistoricInstanceDTO> processHistoricInstanceDTOS = listHisTaskInst();
        Map<String, List<ProcessHistoricInstanceDTO>> map = processHistoricInstanceDTOS.stream().collect(Collectors.groupingBy(ProcessHistoricInstanceDTO::getProcInstId));
        List<String> procInstIds = new ArrayList<>(map.keySet());
        List<ProcessInstance> processInstances = processInstanceService.findByProcInstIdIn(procInstIds);
        return CollectionUtils.isEmpty(processInstances) ? 0 : processInstances.size();
    }

    @Override
    public List<ProcessHistoricInstanceDTO> listHisTaskInst() {
        List<HistoricTaskInstance> allTasks = new ArrayList<>();
        List<HistoricTaskInstance> finishedTasks = historyService.createHistoricTaskInstanceQuery().finished().taskAssignee(LoginUserUtils.getLoginId()).list();

        List<String> nodeKeys = finishedTasks.stream().map(HistoricTaskInstance::getTaskDefinitionKey).collect(toList());

        // 获取所有的第一个节点
        List<ProcessNode> applicantNode = processNodeService.listApplicantNode(nodeKeys);
        // 过滤申请人节点的任务
        if (CollectionUtils.isNotEmpty(applicantNode)) {
            log.info("=========================> 待办 获取申请人节点：{}", JsonUtils.toJson(applicantNode));
            List<String> applicantNodeKeys = applicantNode.stream().map(ProcessNode::getNodeKey).collect(toList());

            finishedTasks.forEach(node -> {
                boolean b = applicantNodeKeys.stream().anyMatch(applicantNodeKey -> StringUtils.equals(node.getTaskDefinitionKey(), applicantNodeKey));
                if (!b) {
                    allTasks.add(node);
                }
            });
        } else {
            allTasks.addAll(finishedTasks);
        }

        List<ProcessHistoricInstanceDTO> result = allTasks.stream().map(task -> {
            ProcessHistoricInstanceDTO dto = new ProcessHistoricInstanceDTO();
            dto.setProcInstId(task.getProcessInstanceId());
            dto.setTaskId(task.getId());
            dto.setClaimFlag(null);
            return dto;
        }).collect(toList());
        log.info("=========================> 待办 所有历史任务：{}", JsonUtils.toJson(result));
        return result;
    }

    @Override
    public Specification<ProcessInstance> buildFilters(ProcessSearchReq req, List<String> proInstIds) {
        List<SearchFilter> searchFilters = new ArrayList<>();
        if (Objects.nonNull(req.getBeginTime())) {
            searchFilters.add(SearchFilter.ge(ProcessInstance::getCreateTime, new Date(req.getBeginTime())));
        }

        if (Objects.nonNull(req.getEndTime())) {
            searchFilters.add(SearchFilter.le(ProcessInstance::getCreateTime, new Date(req.getEndTime())));
        }
        searchFilters.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));
        searchFilters.add(SearchFilter.in(ProcessInstance::getProcInstId, proInstIds));
        searchFilters.add(SearchFilter.notIn(ProcessInstance::getStatus, Arrays.asList(ProcessStatusEnum.UNCOMMITTED.getCode())));

        Specification<ProcessInstance> spec1 = JpaSearchUtils.buildAndSpec(searchFilters);
        Specification<ProcessInstance> spec2 = null;
        Specification<ProcessInstance> spec3 = null;

        if (StringUtils.isNotBlank(req.getKeyword())) {
            spec2 = JpaSearchUtils.buildAndSpec(Collections.singleton(SearchFilter.like(ProcessInstance::getTitle, req.getKeyword())));
            spec3 = JpaSearchUtils.buildAndSpec(Collections.singleton(SearchFilter.equal(ProcessInstance::getProcessNo, req.getKeyword())));
        }
        Specification<ProcessInstance> or = JpaSearchUtils.or(spec2, spec3);
        Specification<ProcessInstance> res = JpaSearchUtils.and(spec1, or);

        return res;
    }

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + ProcessModuleEnum.DONE.getCode();
    }
}
