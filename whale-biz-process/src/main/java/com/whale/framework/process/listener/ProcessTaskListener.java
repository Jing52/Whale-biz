package com.whale.framework.process.listener;

import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.magus.framework.camunda.entity.*;
import com.whale.framework.process.entity.*;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessCompleteRuleEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.factory.ProcessActionStrategyFactory;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.camunda.service.*;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.SpringUtils;
import com.magus.framework.system.api.UserRestApi;
import com.whale.framework.process.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component("processTaskListener")
public class ProcessTaskListener implements TaskListener {

    private static String TIMEOUT_SUBJECT = "超时提醒：";

    HistoryService historyService;

    TaskService taskService;

    ProcessDefinitionService processDefinitionService;

    ProcessNodeService processNodeService;

    ProcessTaskService processTaskService;

    ProcessInstanceService processInstanceService;

    ProcessCCService processCCService;

    ProcessCCInstanceService processCCInstanceService;

    ProcessScheduleService processScheduleService;

    ProcessActionStrategyFactory processActionStrategyFactory;

//    private NoticeRestApi noticeRestApi;

    private UserRestApi userRestApi;

    public ProcessTaskListener() {
        this.historyService = SpringUtils.getBean(HistoryService.class);
        this.taskService = SpringUtils.getBean(TaskService.class);
        this.processDefinitionService = SpringUtils.getBean(ProcessDefinitionService.class);
        this.processNodeService = SpringUtils.getBean(ProcessNodeService.class);
        this.processTaskService = SpringUtils.getBean(ProcessTaskService.class);
        this.processInstanceService = SpringUtils.getBean(ProcessInstanceService.class);
        this.processCCService = SpringUtils.getBean(ProcessCCService.class);
        this.processCCInstanceService = SpringUtils.getBean(ProcessCCInstanceService.class);
        this.processScheduleService = SpringUtils.getBean(ProcessScheduleService.class);
        this.processActionStrategyFactory = SpringUtils.getBean(ProcessActionStrategyFactory.class);
//        this.noticeRestApi = SpringUtils.getBean(NoticeRestApi.class);
        this.userRestApi = SpringUtils.getBean(UserRestApi.class);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        if (TaskListener.EVENTNAME_CREATE.equals(eventName)) {
            ProcessDefinitionRsp procDef = processDefinitionService.findByProcDefId(delegateTask.getProcessDefinitionId());
            // 保存到本地task
            saveTask(delegateTask);

            // 根据同意规则处理非申请人的用户节点
            handleCompleteRule(delegateTask, procDef);

            // 处理抄送的人
            handleCC(delegateTask);

            // 处理超时规则
//            handleSchedule(delegateTask);
        }
        if (TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
        }
        if (TaskListener.EVENTNAME_TIMEOUT.equals(eventName)) {
            log.error("超时");
        }
        if (TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
        }
        if (TaskListener.EVENTNAME_DELETE.equals(eventName)) {
        }
        if (TaskListener.EVENTNAME_UPDATE.equals(eventName)) {
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveTask(DelegateTask delegateTask) {
        log.info("=========================> 保存任务 开始");
        ProcessTask task = new ProcessTask();
        task.setTaskId(delegateTask.getId());
        task.setProcInstId(delegateTask.getProcessInstanceId());
        task.setNodeKey(delegateTask.getTaskDefinitionKey());
        ProcessNode firstUserTask = processNodeService.getFirstUserTask(delegateTask.getProcessDefinitionId());
        if (Objects.isNull(firstUserTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
        // 申请人节点不处理(被驳回的除外)，流程启动并且创建申请人启动后再处理
        ProcessInstance procInst = processInstanceService.findByProcInstId(delegateTask.getProcessInstanceId());
        if (!StringUtils.equals(delegateTask.getTaskDefinitionKey(), firstUserTask.getNodeKey()) || (Objects.nonNull(procInst) && ProcessStatusEnum.REJECT.getCode().equals(procInst.getStatus()))) {
            log.info("=========================> 非申请人节点(被驳回的除外) nodeKey：{}", delegateTask.getTaskDefinitionKey());
            ProcessNodeRsp curNode = processNodeService.findByProcDefIdAndNodeKey(delegateTask.getProcessDefinitionId(), delegateTask.getTaskDefinitionKey());
            if (Objects.isNull(curNode) || (StringUtils.isBlank(curNode.getPageId()) && StringUtils.isBlank(curNode.getPageUri()))) {
                task.setBusinessTableName(procInst.getBusinessTableName());
                task.setBusinessId(procInst.getBusinessId());
            }
        }
        processTaskService.save(task);
        log.info("=========================> 保存任务 结束： {}", JsonUtils.toJson(task));
    }

    /**
     * 处理抄送的人
     *
     * @param delegateTask
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleCC(DelegateTask delegateTask) {
        log.info("=========================> 处理传阅 开始");
        ProcessNodeRsp node = processNodeService.findByProcDefIdAndNodeKey(delegateTask.getProcessDefinitionId(), delegateTask.getTaskDefinitionKey());
        if (BooleanUtils.isNotTrue(node.getCcFlag())) {
            return;
        }

        List<ProcessCC> ccs = processCCService.findByProcDefIdAndNodeKey(delegateTask.getProcessDefinitionId(), delegateTask.getTaskDefinitionKey());

        List<ProcessCCInstance> ccInstances = new ArrayList<>();
        for (ProcessCC cc : ccs) {
            ProcessCCInstance processCCInstance = new ProcessCCInstance();
            processCCInstance.setTaskId(delegateTask.getId());
            processCCInstance.setProcDefId(delegateTask.getProcessDefinitionId());
            processCCInstance.setProcInstId(delegateTask.getProcessInstanceId());
            processCCInstance.setUserId(cc.getUserId());
            processCCInstance.setState(1);
            ccInstances.add(processCCInstance);
        }

        processCCInstanceService.saveAll(ccInstances);
        log.info("=========================> 处理传阅 结束：{}", ccInstances.size());
    }

    /**
     * 根据同意规则处理非申请人的用户节点
     *
     * @param delegateTask
     * @param procDef
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    private void handleCompleteRule(DelegateTask delegateTask, ProcessDefinitionRsp procDef) {
        log.info("=========================> 根据同意规则 处理任务 开始");
        ProcessNode firstUserTask = processNodeService.getFirstUserTask(delegateTask.getProcessDefinitionId());
        if (Objects.isNull(firstUserTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
        if (StringUtils.equals(firstUserTask.getNodeKey(), delegateTask.getTaskDefinitionKey())
                || StringUtils.isBlank(procDef.getCompleteRule())) {
            return;
        }

        // 封装需要调用完成的入参
        ProcessTaskActionReq req = new ProcessTaskActionReq();
        ProcessTaskActionReq.ProcessTerminateActionReq terminateActionReq = new ProcessTaskActionReq.ProcessTerminateActionReq();
        terminateActionReq.setOpinion(ProcessActionEnum.ACTION_COMPLETE.getDesc());

        req.setTerminate(terminateActionReq);
        req.setAction(ProcessActionEnum.ACTION_COMPLETE.getCode());
        req.setProcInstId(delegateTask.getProcessInstanceId());
        req.setTaskId(delegateTask.getId());


        switch (ProcessCompleteRuleEnum.valueOf(procDef.getCompleteRule())) {
            case CLOSE -> {
                // 不启用
                log.info("流程同意规则不启用");
            }
            case START_USER -> {
                // 审批人为发起人
                log.info("=========================> 审批人为发起人 ：{}", JsonUtils.toJson(req));
                ProcessInstance procInst = processInstanceService.findByProcInstId(delegateTask.getProcessInstanceId());
                if (StringUtils.equals(procInst.getStartUserId(), delegateTask.getAssignee())) {
                    processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
                }
            }
            case NEIGHBOR -> {
                // 审批人与上一节点处理人相同
                log.info("=========================> 审批人与上一节点处理人相同 ：{}", JsonUtils.toJson(req));
                List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).taskAssigned().finished().orderByHistoricTaskInstanceEndTime().desc().list();
                if (CollectionUtils.isNotEmpty(hisTasks) && StringUtils.equals(hisTasks.get(0).getAssignee(), delegateTask.getAssignee())) {
                    processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
                }
            }
            case PARTICIPATION -> {
                // 审批人处理过该流程
                log.info("=========================> 审批人处理过该流程 ：{}", JsonUtils.toJson(req));
                List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).taskAssigned().finished().list();
                List<String> hisAssignees = hisTasks.stream().map(HistoricTaskInstance::getAssignee).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(hisAssignees) && hisAssignees.contains(delegateTask.getAssignee())) {
                    processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
                }
            }
            default -> {
                throw new MagusException(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST);
            }
        }
        log.info("=========================> 根据同意规则 处理任务 结束");
    }

//    /**
//     * 处理定时器
//     *
//     * @param delegateTask
//     */
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public void handleSchedule(DelegateTask delegateTask) {
//        log.info("=========================> 处理定时任务 开始");
//        ProcessSchedule processSchedule = processScheduleService.findByProcDefIdAndNodeKey(delegateTask.getProcessDefinitionId(), delegateTask.getTaskDefinitionKey());
//        if (Objects.isNull(processSchedule)) {
//            return;
//        }
//
//        if (ProcessScheduleTimeTypeEnum.CUSTOM.getCode().equals(processSchedule.getTimeType())) {
//            switch (ProcessScheduleHandleRuleEnum.valueOf(processSchedule.getHandleType())) {
//                case AUTO_REMIND -> {
//                    handleAuto(processSchedule,
//                            processSchedule.getAfterTime(),
//                            TimeUnit.valueOf(processSchedule.getAfterUnit()),
//                            () -> autoRemind(delegateTask, processSchedule.getRemindMessage()));
//                }
//                case AUTO_SUBMIT -> {
//                    handleAuto(processSchedule,
//                            processSchedule.getAfterTime(),
//                            TimeUnit.valueOf(processSchedule.getAfterUnit()),
//                            () -> autoSubmit(delegateTask));
//                }
//                case AUTO_REVERSE -> {
//                    handleAuto(processSchedule,
//                            processSchedule.getAfterTime(),
//                            TimeUnit.valueOf(processSchedule.getAfterUnit()),
//                            () -> autoReverse(delegateTask));
//                }
//                default -> {
//                    throw new MagusException(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST);
//                }
//            }
//        } else if (ProcessScheduleTimeTypeEnum.FORM.getCode().equals(processSchedule.getTimeType())) {
//            log.info("=========================> 暂时不处理");
//        }
//        log.info("=========================> 处理定时任务 结束");
//    }

//    /**
//     * 自动提醒
//     *
//     * @param delegateTask
//     * @param remindMessage
//     */
//    private void autoRemind(DelegateTask delegateTask, String remindMessage) {
//        log.info("=========================> 处理定时任务 自动提醒 开始");
//        String currentActivityId = delegateTask.getTaskDefinitionKey();
//        // 获取当前节点的所有待办任务
//        List<Task> tasks = taskService.createTaskQuery()
//                .processInstanceId(delegateTask.getProcessInstanceId())
//                .taskDefinitionKey(currentActivityId)
//                .list();
//
//        List<String> to = new ArrayList<>();
//        List<String> cc = new ArrayList<>();
//        // 发送人
//        List<String> assignees = tasks.stream().map(Task::getAssignee).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(assignees)) {
//            List<UserRsp> assigneeUsers = userRestApi.listUsersByUserId(assignees).getList();
//            to = assigneeUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//        }
//        log.info("=========================> 处理定时任务 自动提醒 发送人：{}", JsonUtils.toJson(to));
//
//        // 抄送人
//        List<String> owners = tasks.stream().map(Task::getOwner).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(owners)) {
//            List<UserRsp> ownerUsers = userRestApi.listUsersByUserId(owners).getList();
//            cc = ownerUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//        }
//        log.info("=========================> 处理定时任务 自动提醒 抄送人：{}", JsonUtils.toJson(cc));
//
//        ProcessInstance processInstance = processInstanceService.findByProcInstId(delegateTask.getProcessInstanceId());
//        if (Objects.isNull(processInstance)) {
//            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
//        }
//
//        NoticeReq req = new NoticeReq();
//        MailReq mailReq = new MailReq();
//        mailReq.setSubject(TIMEOUT_SUBJECT + processInstance.getTitle() + "(" + processInstance.getProcessNo() + ")");
//        mailReq.setContext(remindMessage);
//
//        // 发送邮件
//        if (CollectionUtils.isEmpty(to)) {
//            // 代理组
//            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(delegateTask.getId());
//            List<String> groupIds = identityLinks.stream()
//                    .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getGroupId() != null)
//                    .map(IdentityLink::getGroupId).collect(Collectors.toList());
//            List<UserRsp> assigneeUsers = userRestApi.listAssignee(groupIds).getList();
//            if (CollectionUtils.isNotEmpty(assigneeUsers)) {
//                to = assigneeUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//                log.info("=========================> 处理定时任务 自动提醒 代理组内的发送人：{}", JsonUtils.toJson(to));
//            }
//        }
//
//        mailReq.setTo(to.toArray(new String[0]));
//        mailReq.setCc(cc.toArray(new String[0]));
//
//        req.setMailReq(mailReq);
//        log.info("=========================> 处理定时任务 自动提醒 发送邮件：{}", JsonUtils.toJson(req));
//        noticeRestApi.notice(req);
//
//        log.info("=========================> 处理定时任务 自动提醒 结束");
//    }
//
//    /**
//     * 自动提交
//     *
//     * @param delegateTask
//     */
//    private void autoSubmit(DelegateTask delegateTask) {
//        log.info("=========================> 处理定时任务 自动提交 开始");
//        ProcessTaskActionReq req = new ProcessTaskActionReq();
//        ProcessTaskActionReq.ProcessTerminateActionReq terminateActionReq = new ProcessTaskActionReq.ProcessTerminateActionReq();
//        terminateActionReq.setOpinion(ProcessActionEnum.ACTION_COMPLETE.getDesc());
//
//        req.setTerminate(terminateActionReq);
//        req.setAction(ProcessActionEnum.ACTION_COMPLETE.getCode());
//        req.setProcInstId(delegateTask.getProcessInstanceId());
//        req.setTaskId(delegateTask.getId());
//
//        log.info("=========================> 处理定时任务 自动提交 同意：{}", JsonUtils.toJson(req));
//        processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
//        log.info("=========================> 处理定时任务 自动提交 结束");
//    }
//
//    /**
//     * 自动回退
//     *
//     * @param delegateTask
//     */
//    private void autoReverse(DelegateTask delegateTask) {
//        log.info("=========================> 处理定时任务 自动退回 开始");
//        ProcessTaskActionReq req = new ProcessTaskActionReq();
//        ProcessTaskActionReq.ProcessTerminateActionReq terminateActionReq = new ProcessTaskActionReq.ProcessTerminateActionReq();
//        terminateActionReq.setOpinion(ProcessActionEnum.ACTION_REJECT_FIRST.getDesc());
//
//        req.setTerminate(terminateActionReq);
//        req.setAction(ProcessActionEnum.ACTION_REJECT_FIRST.getCode());
//        req.setProcInstId(delegateTask.getProcessInstanceId());
//        req.setTaskId(delegateTask.getId());
//
//        log.info("=========================> 处理定时任务 自动退回 驳回：{}", JsonUtils.toJson(req));
//        processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
//        log.info("=========================> 处理定时任务 自动退回 结束");
//    }
//
//    /**
//     * 处理自动提醒/提交/退回
//     *
//     * @param processSchedule
//     * @param time
//     * @param unit
//     */
//    private void handleAuto(ProcessSchedule processSchedule, int time, TimeUnit unit, Runnable runnable) {
//        log.info("=========================> 处理自动提醒/提交/退回：{}, time: {}, unit: {}", JsonUtils.toJson(processSchedule), time, unit);
//        switch (ProcessScheduleTimeoutTypeEnum.valueOf(processSchedule.getAutoType())) {
//            case CURRENT_TIME -> {
//                timeoutJob(time, unit, runnable);
//            }
//            case BEFORE_TIME -> {
//                int offset = transfer2Minutes(time, unit) - transfer2Minutes(processSchedule.getOffsetTime(), TimeUnit.valueOf(processSchedule.getOffsetUnit()));
//                timeoutJob(offset, TimeUnit.MINUTES, runnable);
//            }
//            case AFTER_TIME -> {
//                int offset = transfer2Minutes(time, unit) + transfer2Minutes(processSchedule.getOffsetTime(), TimeUnit.valueOf(processSchedule.getOffsetUnit()));
//                timeoutJob(offset, TimeUnit.MINUTES, runnable);
//            }
//            default -> {
//                throw new MagusException(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST);
//            }
//        }
//    }
//
//    // 定时任务
//    public static void timeoutJob(int time, TimeUnit unit, Runnable runnable) {
//        // 在x秒/分/天后执行任务
//        log.info("=========================> 在{}{}后执行任务, time: {}, unit: {}", time, unit);
//        Executors.newScheduledThreadPool(10).schedule(runnable, time, unit);
//    }
//
//    private static int transfer2Minutes(int time, TimeUnit unit) {
//        int result = 0;
//        switch (unit) {
//            case DAYS -> {
//                result = time * 24 * 60;
//            }
//            case HOURS -> {
//                result = time * 60;
//            }
//            case MINUTES -> {
//                result = time;
//            }
//            default -> {
//                log.warn("暂时不支持！！！");
//            }
//        }
//        return result;
//    }

}