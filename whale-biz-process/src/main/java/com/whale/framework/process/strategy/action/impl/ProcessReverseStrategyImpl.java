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
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.strategy.impl.action
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
public class ProcessReverseStrategyImpl implements IProcessActionStrategy {

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
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_REVERSE.getCode();
    }

    /**
     *  撤回
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_REVERSE)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 撤回 入参：{}", JsonUtils.toJson(req));
        // 流程实例 camunda官方重启也是将旧实例删除
        Task task = preExecute(req, taskService);

        ProcessInstance processInstance = this.processInstanceService.findByProcInstId(task.getProcessInstanceId());
        if (Objects.nonNull(processInstance)) {
            // 本地流程实例变更为未提交状态
            processInstance.setRejectNodeKey(task.getTaskDefinitionKey());
            processInstance.setProcInstId(null);
            processInstance.setStatus(ProcessStatusEnum.UNCOMMITTED.getCode());
            this.processInstanceService.save(processInstance);

            // 获取流程定义
            ProcessNode firstUserTask = processNodeService.getFirstUserTask(processInstance.getProcDefId());
            if (Objects.isNull(firstUserTask)) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
            }

            // 删除流程实例
            log.info("=========================> 撤回 流程实例删除");
            runtimeService.deleteProcessInstance(req.getProcInstId(), ProcessActionEnum.ACTION_REVERSE.getDesc());
        }
        log.info("=========================> 撤回 结束");
    }
}
