package com.whale.framework.process.strategy.module.impl;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.whale.framework.process.dto.ProcessHistoricInstanceDTO;
import com.whale.framework.process.dto.ProcessIdentityLinkDTO;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.service.ProcessInstanceService;
import com.whale.framework.process.service.ProcessTaskService;
import com.whale.framework.process.strategy.module.IProcessStrategy;
import com.whale.framework.process.templates.BaseProcessTodoOrDoneTemplate;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
public class ProcessTodoStrategyImpl extends BaseProcessTodoOrDoneTemplate implements IProcessStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessTaskService processTaskService;

    @Override
    public Integer count() {
        log.info("=========================> 待办 所有的任务计数 开始");
        // 获取所有历史任务实例
        List<ProcessHistoricInstanceDTO> tasks = listHisTaskInst();

        // 获取所有的流程实例
        List<String> proInstIds = tasks.stream().map(ProcessHistoricInstanceDTO::getProcInstId).distinct().collect(toList());

        Specification<ProcessInstance> camundaProcessInstanceSpecification = buildFilters(new ProcessSearchReq(), proInstIds);
        List<ProcessInstance> all = processInstanceService.findAll(camundaProcessInstanceSpecification);
        return CollectionUtils.isEmpty(all) ? 0 : all.size();
    }

    @Override
    public List<ProcessHistoricInstanceDTO> listHisTaskInst() {
        List<ProcessHistoricInstanceDTO> allTasks = new ArrayList<>();
        // 查询出指定人的任务
        List<Task> userTasks = taskService.createTaskQuery().taskAssignee(LoginUserUtils.getLoginId()).active().list();
        userTasks.forEach(task -> {
            ProcessHistoricInstanceDTO dto = new ProcessHistoricInstanceDTO();
            dto.setProcInstId(task.getProcessInstanceId());
            dto.setTaskId(task.getId());
            dto.setClaimFlag(Boolean.FALSE);
            dto.setDelegateFlag(DelegationState.PENDING == task.getDelegationState());
            allTasks.add(dto);
        });

        // 组的处理
        allTasks.addAll(groupTasks());
        log.info("=========================> 待办 所有的历史任务：{}", JsonUtils.toJson(allTasks));
        return allTasks;
    }

    private List<ProcessHistoricInstanceDTO> groupTasks() {
        log.info("=========================> 查询待办 代理组任务 开始");
        List<ProcessHistoricInstanceDTO> groupTasks = new ArrayList<>();
        List<String> groupIds = processTaskService.getRuTaskGroupId(LoginUserUtils.getLoginId());
        if (CollectionUtils.isNotEmpty(groupIds)) {
            // 1. 查询link表，查询组织的task
            List<ProcessIdentityLinkDTO> links = processTaskService.listGroupTaskByGroupId(groupIds);

            // 2. 根据taskId查询任务的信息
            List<String> taskIds = links.stream().map(ProcessIdentityLinkDTO::getTaskId).collect(Collectors.toList());
            String[] taskIdStrs = taskIds.toArray(new String[taskIds.size()]);
            List<Task> tasks = taskService.createTaskQuery().taskIdIn(taskIdStrs).list();
            log.info("=========================> 待办 所有代理组对应的任务：{}", tasks.size());
            Map<String, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, Function.identity(), (x, y) -> x));

            // 转换数据
            links.forEach(link -> {
                Task task = Optional.ofNullable(taskMap.get(link.getTaskId())).orElse(taskService.newTask());
                if (groupTasks.stream().anyMatch(x -> StringUtils.equals(x.getProcInstId(), task.getProcessInstanceId()))) {
                    return;
                }
                ProcessHistoricInstanceDTO dto = new ProcessHistoricInstanceDTO();
                dto.setProcInstId(task.getProcessInstanceId());
                dto.setTaskId(link.getTaskId());
                dto.setClaimFlag(Boolean.TRUE);
                dto.setDelegateFlag(Boolean.FALSE);
                groupTasks.add(dto);
            });
        }
        log.info("=========================> 待办 代理组任务详情：{}", JsonUtils.toJson(groupTasks));
        return groupTasks;
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
        searchFilters.add(SearchFilter.equal(ProcessInstance::getStatus, ProcessStatusEnum.PENDING.getCode()));

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
        return CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + ProcessModuleEnum.TODO.getCode();
    }
}