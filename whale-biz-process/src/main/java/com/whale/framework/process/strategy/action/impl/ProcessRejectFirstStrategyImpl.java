package com.whale.framework.process.strategy.action.impl;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.entity.ProcessNode;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.service.ProcessInstanceService;
import com.whale.framework.process.service.ProcessNodeService;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import io.seata.spring.annotation.GlobalTransactional;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.strategy.impl.action
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
public class ProcessRejectFirstStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessNodeService processNodeService;

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_REJECT_FIRST.getCode();
    }

    /**
     * 驳回至原点操作
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_REJECT_FIRST)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 驳回至原点 入参：{}", JsonUtils.toJson(req));
        // 流程实例 camunda官方重启也是将旧实例删除
        Task task = preExecute(req, taskService);

        ProcessInstance processInstance = this.processInstanceService.findByProcInstId(task.getProcessInstanceId());
        if (Objects.nonNull(processInstance)) {
            processInstance.setRejectNodeKey(task.getTaskDefinitionKey());
            processInstance.setStatus(ProcessStatusEnum.REJECT.getCode());
            this.processInstanceService.save(processInstance);

            opinion(task, taskService, req.getTerminate().getOpinion());

            // 获取流程定义
            ProcessNode firstUserTask = processNodeService.getFirstUserTask(processInstance.getProcDefId());
            if (Objects.isNull(firstUserTask)) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
            }

            // 查询所有正在运行的任务
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcInstId()).list();

            log.info("=========================> 驳回至原点 所有正在运行的任务：{}", tasks.size());
            ProcessInstanceModificationBuilder builder = runtimeService.createProcessInstanceModification(task.getProcessInstanceId());

            Set<String> activityIdSet = new HashSet<>();
            tasks.forEach(t -> {
                if (Objects.nonNull(t.getDelegationState()) && DelegationState.PENDING == t.getDelegationState()) {
                    // 委托状态为空或者已完成委办,才可以完成
                    this.taskService.resolveTask(task.getId());
                }
                String activityId = t.getTaskDefinitionKey();
                if (activityIdSet.add(activityId)) {
                    builder.cancelAllForActivity(t.getTaskDefinitionKey());
                }
            });
            // 退回至起点
            builder.startBeforeActivity(firstUserTask.getNodeKey())
                    .setAnnotation(ProcessActionEnum.ACTION_REJECT_FIRST.getDesc())
                    .execute();
        }
        log.info("=========================> 驳回至原点 结束");
    }
}
