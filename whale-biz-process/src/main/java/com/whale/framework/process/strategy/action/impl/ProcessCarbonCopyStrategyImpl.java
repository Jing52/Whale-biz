package com.whale.framework.process.strategy.action.impl;

import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.whale.framework.process.entity.ProcessCCInstance;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.service.ProcessCCInstanceService;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import io.seata.spring.annotation.GlobalTransactional;
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
 * @Description: 传阅
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
public class ProcessCarbonCopyStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    ProcessCCInstanceService processCCInstanceService;

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_CARBON_COPY.getCode();
    }

    /**
     * 传阅
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 传阅 入参：{}", JsonUtils.toJson(req));
        Task task = preExecute(req, taskService);
        if (Objects.nonNull(task.getDelegationState()) && DelegationState.PENDING == task.getDelegationState()) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_UNABLE_CC);
        }
        List<ProcessCCInstance> list = processCCInstanceService.findByProcInstId(req.getProcInstId());
        List<ProcessCCInstance> loseEfficacy = list.stream().map(x -> {
            x.setState(0);
            return x;
        }).collect(Collectors.toList());
        log.info("=========================> 传阅 将历史抄送置为失效：{}", JsonUtils.toJson(loseEfficacy));
        processCCInstanceService.saveAll(loseEfficacy);


        ProcessCCInstance processCCInstance = new ProcessCCInstance();
        processCCInstance.setTaskId(req.getTaskId());
        processCCInstance.setProcDefId(task.getProcessDefinitionId());
        processCCInstance.setProcInstId(req.getProcInstId());
        processCCInstance.setUserId(req.getCc().getCcId());
        processCCInstance.setState(1);

        processCCInstanceService.save(processCCInstance);
        log.info("=========================> 传阅 结束");
    }
}
