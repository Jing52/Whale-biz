package com.whale.framework.process.strategy.action.impl;

import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.service.ProcessDefinitionService;
import com.whale.framework.process.service.ProcessDelegateService;
import com.whale.framework.process.service.ProcessTaskService;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.strategy.impl.action
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
public class ProcessDelegateStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessDelegateService processDelegateService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_DELEGATE.getCode();
    }

    /**
     * 委托
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
//    @ProcessActivity(action = ProcessActionEnum.ACTION_DELEGATE)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 委托 入参：{}", JsonUtils.toJson(req));
        Task task = preExecute(req, taskService);
        // 代理人无法委托
        if (Objects.nonNull(task.getDelegationState()) && DelegationState.PENDING == task.getDelegationState()) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_AGENT_UNABLE_ENTRUST);
        }

        // 代理人也是当前节点的处理人之一,无法委托
        List<Task> assigneeTasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        assigneeTasks = assigneeTasks.stream().filter(t -> StringUtils.isNotBlank(t.getAssignee())).collect(Collectors.toList());
        List<String> assignees = assigneeTasks.stream().map(Task::getAssignee).collect(Collectors.toList());
        log.info("=========================> 委托 任务处理人：{}", JsonUtils.toJson(assignees));
        if (assignees.contains(req.getDelegate().getAssignee())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_EXIST);
        }
        List<Task> otherTasks = assigneeTasks.stream().filter(t -> StringUtils.isBlank(t.getAssignee())).collect(Collectors.toList());
        List<String> taskIds = otherTasks.stream().map(Task::getId).collect(Collectors.toList());
        List<String> groupUserIds = processTaskService.findGroupUser(taskIds);
        if (groupUserIds.contains(req.getDelegate().getAssignee())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_EXIST);
        }

        // 委办 需要查询代理人是否委托了他人代理
        String assignee = req.getDelegate().getAssignee();

        ProcessDefinitionRsp def = processDefinitionService.findByProcDefId(task.getProcessDefinitionId());
        if (Objects.isNull(def)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(def.getProcDefKey()).getData();
        if (Objects.isNull(sysGroup)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }
        ProcessDelegate delegate = processDelegateService.getTargetDelegate(sysGroup.getId(), assignee);
        if (StringUtils.isNotBlank(delegate.getAssignee())) {
            assignee = delegate.getAssignee();
        }
        log.info("=========================> 委托 委托操作，最终委托人:{}", assignee);
        taskService.delegateTask(req.getTaskId(), assignee);
        log.info("=========================> 委托 结束");
    }
}
