package com.whale.framework.process.strategy.action.impl;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.entity.ProcessTask;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.camunda.service.*;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.generator.api.GeneratorCommonRestApi;
import com.magus.framework.generator.api.GeneratorGroupRestApi;
import com.magus.framework.generator.api.dto.rsp.GeneratorCommonUpsertRsp;
import com.magus.framework.generator.api.dto.rsp.GeneratorGroupRsp;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import com.whale.framework.process.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.strategy.impl.action
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
@Slf4j
public class ProcessCompletedStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessDelegateService processDelegateService;

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Autowired
    GeneratorGroupRestApi generatorGroupRestApi;

    @Autowired
    GeneratorCommonRestApi commonRestApi;

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_COMPLETE.getCode();
    }

    /**
     * 同意
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_COMPLETE)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 同意 入参：{}", JsonUtils.toJson(req));
        // 前置操作
        Task task = preExecute(req, taskService);

        // 处理节点数据
        handleBusinessData(req, task);

        // 处理委托逻辑
        handleDelegate(req, task);

        // 处理意见
        opinion(task, taskService, req.getTerminate().getOpinion());

        // 后置操作
        afterExecute(req);
    }

    /**
     * 处理节点数据
     *
     * @param req
     * @param task
     */
    private void handleBusinessData(ProcessTaskActionReq req, Task task) {
        log.info("=========================> 处理节点业务数据 入参：{}, {}", JsonUtils.toJson(req), JsonUtils.toJson(task.getId()));
        ProcessTask processTask = processTaskService.findByTaskId(task.getId());

        ProcessInstance processInstance = processInstanceService.findByProcInstId(task.getProcessInstanceId());
        String tableName = processInstance.getBusinessTableName();
        String id = processInstance.getBusinessId();
        if (CollectionUtils.isEmpty(req.getData())) {
            processTask.setBusinessTableName(tableName);
            processTask.setBusinessId(id);
            processTaskService.save(processTask);
            return;
        }

        // 处理节点数据 1. 当前节点为空 不处理，2. 当前节点页面和主流程一致：更新 3. 不一致:保存
        ProcessDefinitionRsp procDef = processDefinitionService.findByProcDefId(task.getProcessDefinitionId());
        if (Objects.isNull(procDef)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        ProcessNodeRsp nodeDef = processNodeService.findByProcDefIdAndNodeKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (Objects.isNull(nodeDef)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
        if (Objects.nonNull(nodeDef.getPageFlag())) {
            AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(procDef.getProcDefKey()).getData();
            GeneratorGroupRsp group = generatorGroupRestApi.findBySysGroupId(sysGroup.getId()).getData();
            if (StringUtils.equals(procDef.getPageId(), nodeDef.getPageId())) {
                log.info("=========================> 处理节点业务数据 更新：{}, {}", group.getId(), JsonUtils.toJson(req.getData()));
                // 更新
                commonRestApi.update(group.getId(), req.getData());
            } else {
                log.info("=========================> 处理节点业务数据 保存：{}, {}", group.getId(), JsonUtils.toJson(req.getData()));
                // 保存
                List<GeneratorCommonUpsertRsp> list = commonRestApi.create(group.getId(), req.getData()).getList();
                if (CollectionUtils.isEmpty(list)) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_DATA_NOT_EXIST);
                }
                GeneratorCommonUpsertRsp generatorCommonUpsertRsp = list.get(0);
                tableName = generatorCommonUpsertRsp.getTable();
                id = generatorCommonUpsertRsp.getPrimaryInfo().getValue();
            }
            processTask.setBusinessTableName(tableName);
            processTask.setBusinessId(id);
            log.info("=========================> 处理节点业务数据 任务保存：{}", JsonUtils.toJson(processTask));
            processTaskService.save(processTask);
        }
    }

    /**
     * 当前任务处理委托状态的处理
     *
     * @param req
     * @param task
     */
    private void handleDelegate(ProcessTaskActionReq req, Task task) {
        log.info("=========================> 当前任务处理委托状态的处理");
        // 委托状态
        DelegationState delegationState = task.getDelegationState();
        if (Objects.nonNull(delegationState) && DelegationState.PENDING == delegationState) {
            // 委托状态为空或者已完成委办,才可以完成
            if (MapUtils.isEmpty(req.getTerminate().getVariables())) {
                this.taskService.resolveTask(task.getId());
            } else {
                this.taskService.resolveTask(task.getId(), req.getTerminate().getVariables());
            }
        }

        // 委托状态为空或者已完成委办,才可以完成
        if (MapUtils.isEmpty(req.getTerminate().getVariables())) {
            this.taskService.complete(task.getId());
        } else {
            this.taskService.complete(task.getId(), req.getTerminate().getVariables());
        }
    }

    /**
     * 完成后的处理
     *
     * @param req
     */
    private void afterExecute(ProcessTaskActionReq req) {
        log.info("=========================> 完成后的处理");
        // 更新完成时间
        ProcessInstance instance = this.processInstanceService.findByProcInstId(req.getProcInstId());

        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().processInstanceId(req.getProcInstId()).finished().list();
        // 如果流程结束，更新本地状态
        if (CollectionUtils.isNotEmpty(instances)) {
            instance.setStatus(ProcessStatusEnum.COMPLETED.getCode());
            log.info("=========================> 完成后的处理 更新本地状态:{}", JsonUtils.toJson(instance));
            this.processInstanceService.save(instance);
        } else {
            // 判断是否是委托状态
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getProcInstId()).active().taskAssigned().list();
            log.info("=========================> 完成后的处理 委托:{}", tasks.size());
            tasks.forEach(targetTask -> processDelegateService.delegate(targetTask));
        }
    }
}
