package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.req.ProcessActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessActionRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeActionRsp;
import com.whale.framework.process.entity.ProcessAction;
import com.whale.framework.process.entity.ProcessNodeAction;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.repository.ProcessActionRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2023/5/4 10:31 AM
 */
@Service
@Slf4j
public class ProcessActionService extends BaseService<ProcessAction, String> {

    @Autowired
    ProcessActionRepository repository;

    @Autowired
    ProcessNodeActionService processNodeActionService;

    @Autowired
    TaskService taskService;

    /**
     * 获取操作权限
     *
     * @param taskId
     * @return
     */
    public List<ProcessNodeActionRsp> listByTaskId(String taskId) {
        log.info("=========================> 根据任务id查询对应节点的操作权限 入参：{}", JsonUtils.toJson(taskId));
        if (StringUtils.isBlank(taskId)) {
            return new ArrayList<>();
        }
        // 获取任务节点
        Task task = taskService.createTaskQuery().taskId(taskId).active().taskAssignee(LoginUserUtils.getLoginId()).singleResult();
        if (Objects.isNull(task)) {
            return new ArrayList<>();
        }
        log.info("=========================> 根据任务id查询对应节点的操作权限 任务：{}", task.getId());
        // 任务key对应的权限
        List<ProcessNodeAction> nodeActions = processNodeActionService.findByNodeKey(task.getTaskDefinitionKey());
        if (CollectionUtils.isEmpty(nodeActions)) {
            return new ArrayList<>();
        }
        log.info("=========================> 根据任务id查询对应节点的操作权限 节点操作：{}", JsonUtils.toJson(nodeActions));
        // 任务操作权限详情
        List<String> actionCodes = nodeActions.stream().map(ProcessNodeAction::getActionCode).collect(Collectors.toList());
        List<ProcessAction> actions = repository.findByCodeInAndDeletedIsFalse(actionCodes);
        log.info("=========================> 根据任务id查询对应节点的操作权限 节点操作code对应信息：{}", JsonUtils.toJson(actions));
        Map<String, ProcessAction> actionMap = actions.stream().collect(Collectors.toMap(ProcessAction::getCode, Function.identity(), (x, y) -> x));

        List<ProcessNodeActionRsp> nodeActionList = new ArrayList<>();
        for (ProcessNodeAction nodeAction : nodeActions) {
            ProcessNodeActionRsp processNodeActionRsp = MagusUtils.copyProperties(nodeAction, ProcessNodeActionRsp.class);
            ProcessAction processAction = Optional.ofNullable(actionMap.get(nodeAction.getActionCode())).orElse(null);
            ProcessActionRsp processActionRsp = MagusUtils.copyProperties(processAction, ProcessActionRsp.class);

            processNodeActionRsp.setAction(processActionRsp);
            nodeActionList.add(processNodeActionRsp);
        }
        log.info("=========================> 根据任务id查询对应节点的操作权限 任务操作权限信息：{}", JsonUtils.toJson(nodeActionList));
        return nodeActionList;
    }

    /**
     * 获取系统默认操作
     *
     * @param actions
     * @return
     */
    public List<ProcessAction> findSystemAction(List<ProcessActionEnum> actions) {
        if (CollectionUtils.isEmpty(actions)) {
            return new ArrayList<>();
        }
        List<String> codes = actions.stream().map(ProcessActionEnum::name).collect(Collectors.toList());
        return repository.findByCodeInAndDeletedIsFalse(codes);
    }

    /**
     * 保存节点的操作
     *
     * @param actions
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveAll(List<ProcessActionReq> actions) {
        log.info("=========================> 保存节点的操作 入参：{}", JsonUtils.toJson(actions));
        // 保存新增的操作
        List<ProcessAction> processActions = MagusUtils.copyList(actions, ProcessAction.class);
        List<ProcessAction> saveActions = processActions.stream().filter(action -> BooleanUtils.isFalse(action.getSysFlag())).distinct().collect(toList());
        log.info("=========================> 保存节点的操作 非系统操作：{}", JsonUtils.toJson(saveActions));


        List<ProcessAction> existActions = this.findExistActions(saveActions);
        log.info("=========================> 保存节点的操作 已存在的操作：{}", JsonUtils.toJson(existActions));
        if (CollectionUtils.isNotEmpty(existActions)) {
            saveActions.removeAll(existActions);
        }
        log.info("=========================> 保存节点的操作 待保存的操作：{}", JsonUtils.toJson(saveActions));
        this.saveAll(saveActions);
    }

    /**
     * 查找已经存在的操作
     *
     * @param actions
     * @return
     */
    public List<ProcessAction> findExistActions(List<ProcessAction> actions) {
        List<ProcessAction> existActions = new ArrayList<>();
        if (CollectionUtils.isEmpty(actions)) {
            return existActions;
        }
        List<String> codes = actions.stream().map(ProcessAction::getCode).collect(Collectors.toList());
        List<ProcessAction> localActions = repository.findByCodeInAndDeletedIsFalse(codes);
        if (CollectionUtils.isNotEmpty(localActions)) {
            log.info("=========================> 查找已经存在的操作 本地所有已存在的操作：{}", JsonUtils.toJson(localActions));
            Map<String, String> actionMap = localActions.stream().collect(Collectors.toMap(ProcessAction::getCode, ProcessAction::getName, (x, y) -> x));
            actions.forEach(action -> {
                String existName = Optional.ofNullable(actionMap.get(action.getCode())).orElse(null);
                if (StringUtils.equals(action.getName(), existName)) {
                    existActions.add(action);
                } else {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_CC_ID_ERROR);
                }
            });
        }
        log.info("=========================> 查找已经存在的操作 本地已存在的操作 最终结果：{}", JsonUtils.toJson(existActions));
        return existActions;
    }
}
