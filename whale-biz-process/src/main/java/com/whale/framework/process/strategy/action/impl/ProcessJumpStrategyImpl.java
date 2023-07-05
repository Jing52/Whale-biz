package com.whale.framework.process.strategy.action.impl;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.service.ProcessDelegateService;
import com.whale.framework.process.service.ProcessExtService;
import com.whale.framework.process.service.ProcessNodeService;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.dto.rsp.UserRsp;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
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
public class ProcessJumpStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    ProcessDelegateService processDelegateService;

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    ProcessExtService processExtService;

    @Autowired
    UserRestApi userRestApi;

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_JUMP.getCode();
    }

    /**
     * 跳转
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_JUMP)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 跳转 入参：{}", JsonUtils.toJson(req));
        Task task = preExecute(req, taskService);
        if (Objects.nonNull(task.getDelegationState()) && DelegationState.PENDING == task.getDelegationState()) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_UNABLE_JUMP);
        }
        // 跳转
        ProcessInstanceModificationBuilder builder = runtimeService.createProcessInstanceModification(task.getProcessInstanceId());

        // 取消节点定义的所有任务
        Set<String> activityIdSet = new HashSet<>();
        taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).active().list().forEach(taskQuery -> {
            String activityId = taskQuery.getTaskDefinitionKey();
            if (activityIdSet.add(activityId)) {
                builder.cancelAllForActivity(activityId);
            }
        });
        // 从目标节点开始
        ProcessNodeRsp targetNode = processNodeService.findByProcDefIdAndNodeKey(task.getProcessDefinitionId(), req.getJump().getTargetNodeKey());
        if (StringUtils.equals(ActivityTypes.MULTI_INSTANCE_BODY, targetNode.getNodeType())) {
            // 多实例节点 会重复生成数据,并且生成了代理组
            VariableMap variables = processExtService.getExtVariablesByNode(targetNode.getProcDefId(), targetNode.getNodeKey());
            List<String> assignees = (List<String>) variables.get(req.getJump().getTargetNodeKey() + ProcessExtService.ASSIGNEE_LIST);
            if (CollectionUtils.isNotEmpty(assignees)) {
                for (String assignee : assignees) {
                    builder.startBeforeActivity(req.getJump().getTargetNodeKey())
                            .setVariable(req.getJump().getTargetNodeKey() + "_assignee", assignee);
                }
            }
            List<String> groups = (List<String>) variables.get(req.getJump().getTargetNodeKey() + ProcessExtService.CANDIDATE_GROUP);
            if (CollectionUtils.isNotEmpty(groups)) {
                List<UserRsp> users = userRestApi.listAssignee(groups).getList();
                for (UserRsp user : users) {
                    builder.startBeforeActivity(req.getJump().getTargetNodeKey())
                            .setVariable(req.getJump().getTargetNodeKey() + "_assignee", user.getLoginId());
                }
            }
        } else {
            builder.startBeforeActivity(req.getJump().getTargetNodeKey());
        }
        builder.execute();

        // 目标节点存在委托配置，进行委托
        List<Task> targetTasks = taskService.createTaskQuery().taskDefinitionKey(req.getJump().getTargetNodeKey()).processInstanceId(task.getProcessInstanceId()).taskAssigned().list();
        log.info("=========================> 跳转 存在委托：{}", JsonUtils.toJson(targetTasks.size()));
        targetTasks.forEach(targetTask -> processDelegateService.delegate(targetTask));
        log.info("=========================> 跳转 结束");
    }
}
