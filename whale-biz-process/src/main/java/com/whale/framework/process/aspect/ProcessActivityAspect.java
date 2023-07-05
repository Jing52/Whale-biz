package com.whale.framework.process.aspect;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.whale.framework.process.dto.ProcessIdentityLinkDTO;
import com.whale.framework.process.dto.ProcessUserDTO;
import com.magus.framework.camunda.entity.*;
import com.whale.framework.process.entity.*;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessActivityStatusEnum;
import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.camunda.service.*;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.system.api.DutyRestApi;
import com.magus.framework.system.api.OrganizationRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.WorkgroupRestApi;
import com.magus.framework.system.api.dto.rsp.DutyRsp;
import com.magus.framework.system.api.dto.rsp.OrganizationRsp;
import com.magus.framework.system.api.dto.rsp.UserRsp;
import com.magus.framework.system.api.dto.rsp.WorkgroupRsp;
import com.whale.framework.process.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @program: magus-engine
 * @description: 设置初始化端点
 * @packagename: com.magus.framework.auth.aspect
 * @author: Mr.Jing
 * @date: 2022-11-04 09:46:48
 **/
@Component
@Aspect
@Slf4j
public class ProcessActivityAspect {

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    ProcessActivityRecordService processActivityRecordService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    ProcessInstanceAppendixRtService processInstanceAppendixRtService;

    @Autowired
    HistoryService historyService;

    @Autowired
    TaskService taskService;

    @Autowired
    WorkgroupRestApi workgroupRestApi;

    @Autowired
    OrganizationRestApi organizationRestApi;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    DutyRestApi dutyRestApi;


    @Pointcut(value = "@annotation(com.magus.framework.camunda.annotation.ProcessActivity)")
    public void point() {
    }

    @AfterReturning(value = "point()", returning = "returnValue")
    public void after(JoinPoint proceedingJoinPoint, Object returnValue) {
        String strClassName = proceedingJoinPoint.getTarget().getClass().getName();
        String strMethodName = proceedingJoinPoint.getSignature().getName();

        //获取注解参数
        MethodSignature sign = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = sign.getMethod();
        ProcessActivity activity = method.getAnnotation(ProcessActivity.class);
        ProcessActionEnum action = activity.action();
        log.debug("{}：[类名]:{},[方法]:{}", action.getCode(), strClassName, strMethodName);

        Object[] args = proceedingJoinPoint.getArgs();
        keepActivityLog(action, args, returnValue);
    }

    private void keepActivityLog(ProcessActionEnum action, Object[] args, Object returnValue) {
        if (null == action || null == args || args.length == 0) {
            return;
        }

        switch (action) {
            case ACTION_SUBMIT -> {
                if (returnValue instanceof ProcessInstance) {
                    log.info("=====================>ACTION_SUBMIT： 日志开始");
                    ProcessInstance out = (ProcessInstance) returnValue;
                    if (StringUtils.isBlank(out.getId())) {
                        return;
                    }
                    String procInstId = out.getProcInstId();
                    ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
                    if (Objects.nonNull(hisActivity) && StringUtils.isNotBlank(procInstId)) {
                        log.info("=====================>ACTION_SUBMIT：更新存在的历史活动 {}", JsonUtils.toJson(hisActivity));
                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.SUBMIT, hisActivity.getTaskId(), LoginUserUtils.getLoginId(), LoginUserUtils.getUserName(), hisActivity.getOpinion(), hisActivity.getAppendixIds(), hisActivity.getSort());
                    } else {
                        log.info("=====================>ACTION_SUBMIT：添加活动动态");
                        // 提交申请记录
                        List<ProcessInstanceAppendixRt> rts = processInstanceAppendixRtService.findByLocalProcInstId(out.getId());
                        String appendixIdsStr = null;
                        if (CollectionUtils.isNotEmpty(rts)) {
                            List<String> appendixIds = rts.stream().map(ProcessInstanceAppendixRt::getAppendixId).collect(Collectors.toList());
                            appendixIdsStr = String.join(",", appendixIds);
                        }

                        ProcessNode firstUserNode = processNodeService.getFirstUserTask(out.getProcDefId());
                        log.info("=====================>ACTION_SUBMIT：申请人节点 {}", JsonUtils.toJson(firstUserNode));
                        List<ProcessTask> tasks = processTaskService.findByProcInstIdAndNodeKey(procInstId, firstUserNode.getNodeKey());
                        if (CollectionUtils.isEmpty(tasks)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_TASK_NOT_FOUND);
                        }

                        upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.SUBMIT, tasks.get(0).getTaskId(), LoginUserUtils.getLoginId(), LoginUserUtils.getUserName(), null, appendixIdsStr, getNextSort(procInstId));
                    }

                    // 节点处理中记录
                    ProcessUserDTO currentOperator = getCurrentOperator(out.getProcInstId());

                    log.info("=====================>ACTION_SUBMIT：新增下一个节点");
                    upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.PENDING, null, currentOperator.getOperatorId(), currentOperator.getOperator(), null, null, getNextSort(procInstId));
                    log.info("=====================>ACTION_SUBMIT：日志结束");
                }
            }
            case ACTION_COMPLETE -> {
                for (Object arg : args) {
                    if (arg instanceof ProcessTaskActionReq) {
                        log.info("=====================>ACTION_COMPLETE： 日志开始");
                        ProcessTaskActionReq in = (ProcessTaskActionReq) arg;
                        if (StringUtils.isBlank(in.getProcInstId())) {
                            return;
                        }
                        String procInstId = in.getProcInstId();

                        ProcessInstance procInst = processInstanceService.findByProcInstId(procInstId);
                        if (Objects.isNull(procInst)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
                        }
                        // 获取历史上调记录
                        ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
                        if (Objects.isNull(hisActivity)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST);
                        }

                        // 获取完成的操作人
                        UserRsp user = getTerminateOperator(procInstId, in.getTaskId());
                        log.info("=====================>ACTION_COMPLETE： 更新历史活动状态 {}", JsonUtils.toJson(hisActivity));
                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.AGREE, in.getTaskId(), user.getLoginId(), user.getUserName(), in.getTerminate().getOpinion(), String.join(",", in.getTerminate().getAppendixIds()), hisActivity.getSort());

                        // 如果流程还未结束，记录下一个操作人的处理中的状态
                        if (ProcessStatusEnum.PENDING.getCode().equals(procInst.getStatus())) {
                            ProcessUserDTO currentOperator = getCurrentOperator(procInstId);
                            log.info("=====================>ACTION_COMPLETE： 流程未结束， 新增活动状态， 获取任务的操作人 => {}", JsonUtils.toJson(currentOperator));
                            upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.PENDING, null, currentOperator.getOperatorId(), currentOperator.getOperator(), null, null, getNextSort(procInstId));
                        } else if (ProcessStatusEnum.COMPLETED.getCode().equals(procInst.getStatus())) {
                            log.info("=====================>ACTION_COMPLETE： 流程已结束， 新增活动状态");
                            upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.COMPLETED, null, null, "", null, null, getNextSort(procInstId));
                        }
                        log.info("=====================>ACTION_COMPLETE：日志结束");
                    }
                }
            }
            case ACTION_CLAIM -> {
                for (Object arg : args) {
                    if (arg instanceof ProcessTaskActionReq) {
                        log.info("=====================>ACTION_CLAIM： 日志开始");
                        ProcessTaskActionReq in = (ProcessTaskActionReq) arg;
                        if (StringUtils.isBlank(in.getProcInstId())) {
                            return;
                        }
                        String procInstId = in.getProcInstId();

                        ProcessInstance procInst = processInstanceService.findByProcInstId(procInstId);
                        if (Objects.isNull(procInst)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
                        }

                        ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
                        if (Objects.isNull(hisActivity)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST);
                        }
                        log.info("=====================>ACTION_CLAIM： 更新历史活动状态 {}", JsonUtils.toJson(hisActivity));
                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.PENDING, in.getTaskId(), LoginUserUtils.getLoginId(), LoginUserUtils.getUserName(), hisActivity.getOpinion(), hisActivity.getAppendixIds(), hisActivity.getSort());
                        log.info("=====================>ACTION_CLAIM：日志结束");
                    }
                }
            }
            case ACTION_REJECT_FIRST -> {
                for (Object arg : args) {
                    if (arg instanceof ProcessTaskActionReq) {
                        log.info("=====================>ACTION_REJECT_FIRST： 日志开始");
                        ProcessTaskActionReq in = (ProcessTaskActionReq) arg;
                        if (StringUtils.isBlank(in.getProcInstId())) {
                            return;
                        }
                        String procInstId = in.getProcInstId();

                        ProcessInstance procInst = processInstanceService.findByProcInstId(procInstId);
                        if (Objects.isNull(procInst)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
                        }

                        // 将处理人状态变更成驳回
                        UserRsp user = getTerminateOperator(procInstId, in.getTaskId());

                        ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
                        if (Objects.isNull(hisActivity)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST);
                        }
                        log.info("=====================>ACTION_REJECT_FIRST： 更新历史活动状态 {}", JsonUtils.toJson(hisActivity));
                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.REJECT, in.getTaskId(), user.getLoginId(), user.getUserName(), in.getTerminate().getOpinion(), String.join(",", in.getTerminate().getAppendixIds()), hisActivity.getSort());

                        // 驳回操作需要将申请人置为处理中
                        log.info("=====================>ACTION_REJECT_FIRST： 新增活动状态");
                        upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.PENDING, null, procInst.getStartUserId(), procInst.getStartUserName(), null, null, getNextSort(procInstId));
                        log.info("=====================>ACTION_REJECT_FIRST：日志结束");
                    }
                }
            }
//            case ACTION_DELEGATE -> {
//                for (Object arg : args) {
//                    if (arg instanceof ProcessTaskActionReq) {
//                        ProcessTaskActionReq in = (ProcessTaskActionReq) arg;
//                        if (StringUtils.isBlank(in.getProcInstId())) {
//                            return;
//                        }
//                        String procInstId = in.getProcInstId();
//
//                        ProcessInstance procInst = processInstanceService.findByProcInstId(procInstId);
//                        if (Objects.isNull(procInst)) {
//                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
//                        }
//
//                        // 将处理人状态变更成委托
//                        ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
//                        if (Objects.isNull(hisActivity)) {
//                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST);
//                        }
//                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.DELEGATE, in.getTaskId(), LoginUserUtils.getLoginId(), LoginUserUtils.getUserName(), hisActivity.getOpinion(), hisActivity.getAppendixIds(), hisActivity.getSort());
//
//                        // 将受让人置为处理中
//                        ProcessUserDTO currentOperator = getCurrentOperator(procInstId);
//                        upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.PENDING, in.getTaskId(), currentOperator.getOperatorId(), currentOperator.getOperator(), null, null, getNextSort(procInstId));
//                    }
//                }
//            }
            case ACTION_JUMP -> {
                for (Object arg : args) {
                    if (arg instanceof ProcessTaskActionReq) {
                        log.info("=====================>ACTION_JUMP： 日志开始");
                        ProcessTaskActionReq in = (ProcessTaskActionReq) arg;
                        if (StringUtils.isBlank(in.getProcInstId())) {
                            return;
                        }
                        String procInstId = in.getProcInstId();

                        ProcessInstance procInst = processInstanceService.findByProcInstId(procInstId);
                        if (Objects.isNull(procInst)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
                        }

                        // 将处理人状态变更成跳转
                        ProcessActivityRecord hisActivity = processActivityRecordService.findLastedPendingActivity(procInstId);
                        if (Objects.isNull(hisActivity)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST);
                        }
                        log.info("=====================>ACTION_JUMP： 更新历史活动状态 {}", JsonUtils.toJson(hisActivity));
                        upsertActivity(hisActivity, procInstId, ProcessActivityStatusEnum.JUMP, in.getTaskId(), hisActivity.getOperatorId(), hisActivity.getOperator(), hisActivity.getOpinion(), hisActivity.getAppendixIds(), hisActivity.getSort());

                        // 将下一个节点置为处理中
                        ProcessUserDTO currentOperator = getCurrentOperator(procInstId);
                        upsertActivity(new ProcessActivityRecord(), procInstId, ProcessActivityStatusEnum.PENDING, in.getTaskId(), currentOperator.getOperatorId(), currentOperator.getOperator(), null, null, getNextSort(procInstId));
                        log.info("=====================>ACTION_JUMP：日志结束");
                    }
                }
            }
            default -> {
                throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
            }
        }

    }

    private UserRsp getTerminateOperator(String procInstId, String taskId) {
        log.info("=====================>获取任务的处理人入参 => procInstId： {}， taskId: {}", procInstId, taskId);
        UserRsp user = new UserRsp();
        String operatorId = null;

        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInstId).taskId(taskId).singleResult();
        if (Objects.nonNull(historicTaskInstance) && StringUtils.isNotBlank(historicTaskInstance.getOwner())) {
            // 委托状态,处理人为OWNER_
            operatorId = historicTaskInstance.getOwner();
        } else {
            // 非委托状态,处理人为ASSIGNEE_
            operatorId = historicTaskInstance.getAssignee();
        }

        if (StringUtils.isNotBlank(operatorId)) {
            user = userRestApi.findByLoginId(operatorId).getData();
        }
        log.info("=====================>获取任务的处理人结果 => user： {}", JsonUtils.toJson(user));
        return user;
    }

    private void upsertActivity(ProcessActivityRecord update
            , String procInstId
            , ProcessActivityStatusEnum status
            , String taskId
            , String operatorId
            , String operator
            , String opinion
            , String appendixIds
            , Integer sort) {
        log.info("=====================>更新/添加 活动节点 {}, procInstId: {}, status: {}, taskId: {}, operatorId: {}, operator: {}, opinion: {}, appendixIds: {}, sort: {}",
                JsonUtils.toJson(update), procInstId, status.getCode(), taskId, operatorId, operator, opinion, JsonUtils.toJson(appendixIds), sort);
        update.setProcInstId(procInstId);
        update.setActivityStatus(status.getCode());
        update.setOperatorId(operatorId);
        update.setOperator(operator);
        update.setTaskId(taskId);
        update.setOpinion(opinion);
        update.setAppendixIds(appendixIds);
        update.setSort(sort);
        processActivityRecordService.save(update);
        log.info("=====================>更新/添加 活动节点成功");
    }

    private Integer getNextSort(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return 0;
        }
        ProcessActivityRecord lastedActivity = processActivityRecordService.findLastedActivity(procInstId);
        return Objects.isNull(lastedActivity) || lastedActivity.getSort() == null ? 0 : lastedActivity.getSort() + 1;
    }

    /**
     * 获取当前任务的操作人
     *
     * @param procInstId
     * @return
     */
    private ProcessUserDTO getCurrentOperator(String procInstId) {
        log.info("=====================>获取流程的当前任务的操作人/组 procInstId： {}", procInstId);
        ProcessUserDTO dto = new ProcessUserDTO();

        StringBuilder operator = new StringBuilder();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        if (CollectionUtils.isEmpty(tasks)) {
            log.error("=============>未获取到任务 procInstId： {}", procInstId);
            return dto;
        }
        // 获取人
        List<String> operatorIds = new ArrayList<>();
        for (Task task : tasks) {
            if (Objects.nonNull(task)) {
                if (DelegationState.PENDING == task.getDelegationState()) {
                    // 委托状态, 处理人为OWNER_
                    String owner = task.getOwner();
                    if (StringUtils.isNotBlank(owner)) {
                        operatorIds.add(owner);
                    }
                } else {
                    // 非委托状态, 处理人为ASSIGNEE_
                    String assignee = task.getAssignee();
                    if (StringUtils.isNotBlank(assignee)) {
                        operatorIds.add(assignee);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(operatorIds)) {
            List<UserRsp> users = userRestApi.listUsersByLoginId(operatorIds).getList();
            users.forEach(user -> operator.append(user.getUserName()).append(" "));
        }
        if (operatorIds.size() == 1) {
            dto.setOperatorId(operatorIds.get(0));
        }

        // 获取组
        // 1. 获取运行中的组
        List<String> taskIds = tasks.stream().map(Task::getId).collect(toList());
        if (CollectionUtils.isNotEmpty(taskIds)) {
            // 1. 查询link
            List<ProcessIdentityLinkDTO> links = processTaskService.listGroupTaskByTaskId(taskIds);
            List<String> groupIds = links.stream().map(ProcessIdentityLinkDTO::getGroupId).collect(toList());
            // 2. 将运行中的组拼接
            if (CollectionUtils.isNotEmpty(groupIds)) {
                List<String> orgIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$ORG.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(orgIds)) {
                    List<String> ids = orgIds.stream().map(x -> StringUtils.removeStart(x, ProcessAssigneeTypeEnum.$ORG.getPre()).split("@")[0]).distinct().collect(toList());
                    List<OrganizationRsp> orgs = organizationRestApi.listOrgByOrgIds(ids).getList();
                    if (CollectionUtils.isNotEmpty(orgs)) {
                        for (OrganizationRsp org : orgs) {
                            operator.append(org.getName()).append(" ");
                        }
                    }
                }
                List<String> workgroupIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$WORKGROUP.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(workgroupIds)) {
                    List<String> ids = workgroupIds.stream().map(x -> StringUtils.removeStart(x, ProcessAssigneeTypeEnum.$WORKGROUP.getPre())).collect(toList());
                    List<WorkgroupRsp> workgroupRspList = workgroupRestApi.listByIds(ids).getList();
                    if (CollectionUtils.isNotEmpty(workgroupRspList)) {
                        for (WorkgroupRsp workgroupRsp : workgroupRspList) {
                            operator.append(workgroupRsp.getName()).append(" ");
                        }
                    }
                }
                List<String> dutyIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$DUTY.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(dutyIds)) {
                    List<String> ids = dutyIds.stream().map(x -> StringUtils.removeStart(x, ProcessAssigneeTypeEnum.$DUTY.getPre())).collect(toList());
                    List<DutyRsp> duties = dutyRestApi.listByIds(ids).getList();
                    if (CollectionUtils.isNotEmpty(duties)) {
                        for (DutyRsp duty : duties) {
                            operator.append(duty.getName()).append(" ");
                        }
                    }
                }
            }
        }
        dto.setOperator(operator.toString());
        log.info("=====================>获取流程的当前任务的操作人/组 operatorId： {}, operator： {}", dto.getOperatorId(), dto.getOperator());
        return dto;
    }

}
