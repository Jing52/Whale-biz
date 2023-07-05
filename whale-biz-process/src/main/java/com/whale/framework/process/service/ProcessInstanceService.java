package com.whale.framework.process.service;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessApplyReq;
import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.*;
import com.whale.framework.process.dto.ProcessHistoricInstanceDTO;
import com.magus.framework.camunda.entity.*;
import com.magus.framework.camunda.enums.*;
import com.whale.framework.process.entity.*;
import com.whale.framework.process.enums.*;
import com.whale.framework.process.factory.ProcessPageStrategyFactory;
import com.whale.framework.process.repository.ProcessInstanceRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.generator.api.GeneratorCommonRestApi;
import com.magus.framework.generator.api.GeneratorGroupRestApi;
import com.magus.framework.generator.api.GeneratorPageRestApi;
import com.magus.framework.generator.api.GeneratorTableRestApi;
import com.magus.framework.generator.api.dto.req.GeneratorCommonReq;
import com.magus.framework.generator.api.dto.rsp.*;
import com.magus.framework.service.BaseService;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.AppendixRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import com.magus.framework.system.api.dto.rsp.AppendixRsp;
import com.magus.framework.system.api.dto.rsp.UserRsp;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.impl
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Service
@Slf4j
public class ProcessInstanceService extends BaseService<ProcessInstance, String> {

    @Autowired
    ProcessInstanceRepository repository;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    ProcessExtService processExtService;

    @Autowired
    ProcessInstanceAppendixRtService processInstanceAppendixRtService;

    @Autowired
    ProcessActivityRecordService processActivityRecordService;

    @Autowired
    ProcessDelegateService processDelegateService;

    @Autowired
    ProcessFieldPermissionService processFieldPermissionService;

    @Autowired
    ProcessCCInstanceService processCCInstanceService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    HistoryService historyService;

    @Autowired
    TaskService taskService;

    @Autowired
    GeneratorGroupRestApi generatorGroupRestApi;

    @Autowired
    GeneratorPageRestApi generatorPageRestApi;

    @Autowired
    GeneratorCommonRestApi generatorCommonRestApi;

    @Autowired
    GeneratorTableRestApi generatorTableRestApi;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    AppendixRestApi appendixRestApi;

    @Autowired
    ProcessPageStrategyFactory processPageStrategyFactory;

    /**
     * 流程统计
     *
     * @return
     */
    public ProcessCountRsp amount() {
        ProcessCountRsp rsp = new ProcessCountRsp();
        for (ProcessModuleEnum module : ProcessModuleEnum.values()) {
            switch (module) {
                case TODO -> {
                    Integer count = processPageStrategyFactory.getStrategy(ProcessModuleEnum.TODO.getCode()).count();
                    rsp.setTodo(count);
                }
                case DONE -> {
                    Integer count = processPageStrategyFactory.getStrategy(ProcessModuleEnum.DONE.getCode()).count();
                    rsp.setDone(count);
                }
                case CC -> {
                    Integer count = processPageStrategyFactory.getStrategy(ProcessModuleEnum.CC.getCode()).count();
                    rsp.setCc(count);
                }
                case READY -> {
                    Integer count = processPageStrategyFactory.getStrategy(ProcessModuleEnum.READY.getCode()).count();
                    rsp.setReady(count);
                }
                case SENT -> {
                    Integer count = processPageStrategyFactory.getStrategy(ProcessModuleEnum.SENT.getCode()).count();
                    rsp.setSent(count);
                }
                case TAB -> {
                    log.warn("暂不支持！！！");
                }
                default -> {
                    log.warn("暂不支持该类型！！！");
                }
            }
        }
        return rsp;
    }

    /**
     * 流程中心分页
     *
     * @param req
     * @return
     */
    public PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req) {
        log.info("=========================> 流程中心分页 入参：{}", JsonUtils.toJson(req));
        if (StringUtils.isBlank(req.getSource())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        PageImpl<ProcessInstanceRsp> page = processPageStrategyFactory.getStrategy(req.getSource()).page(req);
        return page;
    }

    public ProcessInstanceRsp getProcessInfoById(String id) {
        if (StringUtils.isBlank(id)) {
            return new ProcessInstanceRsp();
        }

        // 流程实例
        ProcessInstance processInstance = processInstanceService.findById(id);
        if (Objects.isNull(processInstance)) {
            log.error("===============>" + ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
            return new ProcessInstanceRsp();
        }

        // 流程定义
        ProcessDefinitionRsp processDefinition = processDefinitionService.findByProcDefId(processInstance.getProcDefId());
        if (Objects.isNull(processDefinition)) {
            log.error("===============>" + ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
            return new ProcessInstanceRsp();
        }

        // 附件信息
        List<ProcessInstanceAppendixRt> processInstanceAppendixRts = processInstanceAppendixRtService.findByLocalProcInstId(id);

        List<ProcessAppendixRsp> appendixRsps = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processInstanceAppendixRts)) {
            List<String> appendixIds = processInstanceAppendixRts.stream().map(ProcessInstanceAppendixRt::getAppendixId).distinct().collect(Collectors.toList());
            List<AppendixRsp> appendices = appendixRestApi.list(appendixIds).getList();
            appendixRsps = MagusUtils.copyList(appendices, ProcessAppendixRsp.class);
        }

        // 评论权限
        Boolean commentFlag = Boolean.FALSE;
        if (BooleanUtils.isTrue(LoginUserUtils.getRealFlag()) && BooleanUtils.isTrue(processDefinition.getCommentFlag())) {
            commentFlag = Boolean.TRUE;
        }

        // 查看流程动态和流程图权限
        Boolean viewFlag = Boolean.FALSE;
        if (BooleanUtils.isTrue(LoginUserUtils.getRealFlag()) && BooleanUtils.isTrue(processDefinition.getViewFlag())) {
            viewFlag = Boolean.TRUE;
        }

        // 查询流程状态
        ProcessActivityRecord activity = processActivityRecordService.findLastedActivity(processInstance.getProcInstId());
        if (Objects.isNull(activity) && !ProcessStatusEnum.UNCOMMITTED.getCode().equals(processInstance.getStatus())) {
            log.error("===============>" + ProcessResultEnum.RESULT_ERROR_ACTIVITY_NOT_EXIST.getMessage());
        }

        // 系统工作组
        AppGroupRsp group = groupRestApi.findByProcDefKey(processDefinition.getProcDefKey()).getData();
        if (Objects.isNull(group)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }

        // 获取当前任务
        ProcessHistoricInstanceDTO task = processTaskService.getCurrentTask(processInstance.getProcInstId());

        ProcessTaskRsp ccTaskRsp = MagusUtils.copyProperties(task, ProcessTaskRsp.class);
        if (StringUtils.isBlank(task.getTaskId())) {
            // 当前操作人没有正在运行的任务，1. 已办||已发 查看 详情的话返回最近处理的节点 2. 抄送的详情
            List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstance.getProcInstId())
                    .taskAssignee(LoginUserUtils.getLoginId())
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
            if (CollectionUtils.isNotEmpty(hisTasks)) {
                HistoricTaskInstance historicTaskInstance = hisTasks.get(0);
                task.setProcInstId(historicTaskInstance.getProcessInstanceId());
                task.setNodeKey(historicTaskInstance.getTaskDefinitionKey());
            }
        }

        ProcessDefinitionRsp procDef = processDefinitionService.findByProcDefId(processDefinition.getProcDefId());

        ProcessNodeRsp nodeDef = processNodeService.findByProcDefIdAndNodeKey(processDefinition.getProcDefId(), task.getNodeKey());

        // 意见
        String opinion = null;
        List<Comment> taskComments = taskService.getTaskComments(task.getNodeKey());
        if (taskComments.size() > 0) {
            opinion = taskComments.get(0).getFullMessage();
        }

        List<ProcessFieldPermissionRsp> fieldsPermission = new ArrayList<>();
        if (Objects.isNull(nodeDef) || Objects.isNull(nodeDef.getPageFlag())) {
            // 没有设置页面，查询流程页面设置的权限
            fieldsPermission = processFieldPermissionService.findFieldPermission(processDefinition.getProcDefId(), null);
        } else {
            // 查询当前节点
            fieldsPermission = processFieldPermissionService.findFieldPermission(processDefinition.getProcDefId(), task.getNodeKey());
        }
        // 默认获取主流程的表单和业务数据
        String businessTableName = processInstance.getBusinessTableName();
        String businessId = processInstance.getBusinessId();
        String pageId = procDef.getPageId();

        if (Objects.nonNull(nodeDef) && BooleanUtils.isTrue(nodeDef.getPageFlag())) {
            // 如果节点选择了内部表单，获取节点的表单和业务数据
            ProcessTask nodeInst = processTaskService.findByTaskId(task.getTaskId());
            if (Objects.nonNull(nodeInst)) {
                // 如果
                businessTableName = nodeInst.getBusinessTableName();
                businessId = nodeInst.getBusinessId();
                pageId = nodeDef.getPageId();
            }
        }

        ProcessTaskRsp taskRsp = new ProcessTaskRsp();
        // 页面数据 TODO 目前是拿不到的，需要去拿实例
        pageId = processInstance.getPageId();
        if (StringUtils.isBlank(pageId)) {
            // 组装数据
            taskRsp = ProcessTaskRsp.builder()
                    .taskId(task.getTaskId())
                    .nodeKey(task.getNodeKey())
                    .pageFlag(Boolean.FALSE)
                    .pageUri(processInstance.getPageUri())
                    .build();
        } else {
            GeneratorPageRsp tablePage = generatorPageRestApi.findByRoute(pageId).getData();

            // 业务数据
            GeneratorGroupRsp groupRsp = generatorGroupRestApi.findBySysGroupId(group.getId()).getData();
            List<GeneratorCommonRsp> data = new ArrayList<>();
            if (StringUtils.isNotBlank(businessTableName) && StringUtils.isNotBlank(businessId)) {
                data = generatorCommonRestApi.findById(groupRsp.getId(), businessTableName, businessId).getList();
            }

            // 组装数据
            taskRsp = ProcessTaskRsp.builder()
                    .taskId(task.getTaskId())
                    .nodeKey(task.getNodeKey())
                    .pageFlag(StringUtils.isNotBlank(pageId))
                    .pageId(pageId)
                    .pageUri(Objects.nonNull(tablePage) ? tablePage.getPageUri() : null)
                    .pageDesign(Objects.nonNull(tablePage) ? tablePage.getPageDesign() : null)
                    .data(data)
                    .build();
        }

        ProcessInstanceRsp rsp = ProcessInstanceRsp.builder()
                .id(id)
                .procDefId(processDefinition.getProcDefId())
                .processNo(processInstance.getProcessNo())
                .procInstId(processInstance.getProcInstId())
                .title(processInstance.getTitle())
                .createId(processInstance.getCreateId())
                .startUserId(processInstance.getStartUserId())
                .startUserName(processInstance.getStartUserName())
                .startTime(processInstance.getStartTime())
                .opinion(opinion)
                .claimFlag(task.getClaimFlag())
                .status(ProcessStatusEnum.valueOf(processInstance.getStatus()).getDesc())
                .activityStatus(Objects.isNull(activity) ? null : ProcessActivityStatusEnum.findDesc(activity.getActivityStatus()))
                .businessTableName(processInstance.getBusinessTableName())// 下面部分属于web版才会有，脚手架代码开发人员自己查询封装，可不用
                .businessId(processInstance.getBusinessId())
                .appendices(appendixRsps)
                .sysGroupId(group.getId())
                .commentFlag(commentFlag)
                .viewFlag(viewFlag)
                .fieldsPermission(fieldsPermission)
                .task(taskRsp)
                .build();

        return rsp;
    }

    /**
     * 流程详情
     *
     * @param id
     * @return
     */
    public ProcessInstanceRsp detail(String id) {
        log.info("=========================> 流程详情 入参：{}", id);

        if (StringUtils.isBlank(id)) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }

        // 流程实例
        ProcessInstance processInstance = processInstanceService.findById(id);
        if (Objects.isNull(processInstance)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
        }

        // 流程定义
        ProcessDefinitionRsp processDefinition = processDefinitionService.findByProcDefId(processInstance.getProcDefId());
        if (Objects.isNull(processDefinition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }

        // 获取申请人节点
        ProcessNode firstUserTask = processNodeService.getFirstUserTask(processDefinition.getProcDefId());
        if (Objects.isNull(firstUserTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_APPLICANT_NOT_EXIST);
        }

        // 附件信息
        List<ProcessInstanceAppendixRt> processInstanceAppendixRts = processInstanceAppendixRtService.findByLocalProcInstId(id);
        List<ProcessAppendixRsp> appendixRsps = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processInstanceAppendixRts)) {
            List<String> appendixIds = processInstanceAppendixRts.stream().map(ProcessInstanceAppendixRt::getAppendixId).distinct().collect(Collectors.toList());
            List<AppendixRsp> appendices = appendixRestApi.list(appendixIds).getList();
            appendixRsps = MagusUtils.copyList(appendices, ProcessAppendixRsp.class);
        }

        // 评论权限
        Boolean commentFlag = Boolean.FALSE;
        if (BooleanUtils.isTrue(LoginUserUtils.getRealFlag()) && BooleanUtils.isTrue(processDefinition.getCommentFlag())) {
            commentFlag = Boolean.TRUE;
        }

        // 查看流程动态和流程图权限
        Boolean viewFlag = Boolean.FALSE;
        if (BooleanUtils.isTrue(LoginUserUtils.getRealFlag()) && BooleanUtils.isTrue(processDefinition.getViewFlag())) {
            viewFlag = Boolean.TRUE;
        }

        // 查询流程状态
        ProcessActivityRecord activity = processActivityRecordService.findLastedActivity(processInstance.getProcInstId());
        if (Objects.isNull(activity) && !ProcessStatusEnum.UNCOMMITTED.getCode().equals(processInstance.getStatus())) {
            log.info("=========================> 流程详情 活动动态不存在");
        }

        // 系统工作组 TODO whale 可能删除
        AppGroupRsp group = groupRestApi.findByProcDefKey(processDefinition.getProcDefKey()).getData();
        if (Objects.isNull(group)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }

        // 获取当前任务
        ProcessHistoricInstanceDTO task = processTaskService.getCurrentTask(processInstance.getProcInstId());
        log.info("=========================> 流程详情 当前任务：{}", JsonUtils.toJson(task));

        // 获取最近的任务节点
        ProcessTaskRsp lastTask = handleLastTask(group.getId(), processInstance);
        log.info("=========================> 流程详情 最近的任务：{}", JsonUtils.toJson(lastTask));

        // 获取抄送的节点
        List<ProcessTaskRsp> ccTasks = handleCcPage(group.getId(), processDefinition.getProcDefId(), processInstance);
        log.info("=========================> 流程详情 抄送的任务：{}", JsonUtils.toJson(ccTasks));

        // 查询节点定义信息
        ProcessNodeRsp nodeDef = processNodeService.findByProcDefIdAndNodeKey(processDefinition.getProcDefId(), task.getNodeKey());
        if (Objects.isNull(nodeDef)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }

        // 如果当前人有运行中的任务,查询对应任务的意见，字段权限，表单和业务数据
        String opinion = null;
        List<ProcessFieldPermissionRsp> fieldsPermission = new ArrayList<>();
        ProcessPageRsp pageRsp = new ProcessPageRsp();
        if (Objects.nonNull(task) && StringUtils.isNotBlank(task.getTaskId())) {
            // 字段权限
            String businessTableName = null;
            String businessId = null;
            String pageId = null;
            if (BooleanUtils.isNotTrue(nodeDef.getPageFlag())) {
                // 没有设置页面，查询申请人节点页面
                // 设置的权限
                fieldsPermission = processFieldPermissionService.findFieldPermission(processDefinition.getProcDefId(), firstUserTask.getNodeKey());

                // 表单和业务数据
                List<ProcessTask> processTasks = processTaskService.findByProcInstIdAndNodeKey(processInstance.getProcInstId(), firstUserTask.getNodeKey());
                if (CollectionUtils.isNotEmpty(processTasks)) {
                    ProcessTask processTask = processTasks.get(0);
                    businessTableName = processTask.getBusinessTableName();
                    businessId = processTask.getBusinessId();
                    pageId = firstUserTask.getPageId();
                }
            } else {
                // 查询当前节点
                fieldsPermission = processFieldPermissionService.findFieldPermission(processDefinition.getProcDefId(), nodeDef.getNodeKey());

                // 如果节点选择了内部表单，获取节点的表单和业务数据
                ProcessTask nodeInst = processTaskService.findByTaskId(task.getTaskId());
                if (Objects.nonNull(nodeInst)) {
                    businessTableName = nodeInst.getBusinessTableName();
                    businessId = nodeInst.getBusinessId();
                    pageId = nodeDef.getPageId();

                    // 页面数据 TODO 目前是拿不到的，需要去拿实例
                    pageId = processInstance.getPageId();
                }
            }
            pageRsp = getProcessPage(group.getId(), businessTableName, businessId, pageId, processInstance.getPageUri());

            // 意见
            List<Comment> taskComments = taskService.getTaskComments(task.getNodeKey());
            if (taskComments.size() > 0) {
                opinion = taskComments.get(0).getFullMessage();
            }
        }

        // 组装任务数据
        ProcessTaskRsp taskRsp = ProcessTaskRsp.builder()
                .taskId(task.getTaskId())
                .nodeKey(task.getNodeKey())
                .page(pageRsp)
                .build();

        ProcessInstanceRsp result = ProcessInstanceRsp.builder()
                .id(id)
                .procDefId(processDefinition.getProcDefId())
                .processNo(processInstance.getProcessNo())
                .procInstId(processInstance.getProcInstId())
                .title(processInstance.getTitle())
                .createId(processInstance.getCreateId())
                .startUserId(processInstance.getStartUserId())
                .startUserName(processInstance.getStartUserName())
                .startTime(processInstance.getStartTime())
                .opinion(opinion)
                .claimFlag(task.getClaimFlag())
                .status(ProcessStatusEnum.valueOf(processInstance.getStatus()).getDesc())
                .activityStatus(Objects.isNull(activity) ? null : ProcessActivityStatusEnum.findDesc(activity.getActivityStatus()))
                .appendices(appendixRsps)
                .sysGroupId(group.getId())
                .commentFlag(commentFlag)
                .viewFlag(viewFlag)
                .fieldsPermission(fieldsPermission)
                .curTask(taskRsp)
                .lastTask(lastTask)
                .ccTasks(ccTasks)
                .build();

        log.info("=========================> 流程详情 结果：{}", JsonUtils.toJson(result));
        return result;
    }

    /**
     * 处理最近的任务
     *
     * @param sysGroupId
     * @param processInstance
     * @return
     */
    private ProcessTaskRsp handleLastTask(String sysGroupId, ProcessInstance processInstance) {
        log.info("=========================> 处理最近的任务 入参：{}， {}", sysGroupId, JsonUtils.toJson(processInstance));
        ProcessTaskRsp task = new ProcessTaskRsp();
        // 本人最近处理的任务
        List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstance.getProcInstId())
                .taskAssignee(LoginUserUtils.getLoginId())
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        if (CollectionUtils.isNotEmpty(hisTasks)) {
            HistoricTaskInstance historicTaskInstance = hisTasks.get(0);
            task.setNodeKey(historicTaskInstance.getTaskDefinitionKey());

            // 查询任务信息 TODO whale processInstance.getPageId() 后面改成节点
            ProcessNodeRsp processNode = processNodeService.findByProcDefIdAndNodeKey(historicTaskInstance.getProcessDefinitionId(), historicTaskInstance.getTaskDefinitionKey());
            ProcessPageRsp processPage = getProcessPage(sysGroupId, processInstance.getBusinessTableName(), processInstance.getBusinessId(), processInstance.getPageId(), processNode.getPageUri());
            task.setPage(processPage);
        }
        log.info("=========================> 处理最近的任务 结果：{}", JsonUtils.toJson(task));

        return task;
    }

    /**
     * 处理抄送的节点
     *
     * @param sysGroupId
     * @param procDefId
     * @param processInstance TODO whale 待改
     * @return
     */
    private List<ProcessTaskRsp> handleCcPage(String sysGroupId, String procDefId, ProcessInstance processInstance) {
        log.info("=========================> 处理抄送的节点 入参：{}， {}", sysGroupId, procDefId, JsonUtils.toJson(processInstance));
        // 是否是传阅的详情 如果是传阅的详情，应该如何展示页面
        List<ProcessCCInstance> ccInsts = processCCInstanceService.findCarbonCopySelf(processInstance.getProcInstId());

        List<ProcessTaskRsp> ccTasks = new ArrayList<>();
        for (int i = 0; i < ccInsts.size(); i++) {
            ProcessTask processTask = processTaskService.findByTaskId(ccInsts.get(i).getTaskId());
            String ccNodeKey = processTask.getNodeKey();
            if (i == 0) {
//                ProcessNodeRsp node = processNodeService.findByProcDefIdAndNodeKey(procDefId, ccNodeKey);
                ProcessPageRsp processPage = getProcessPage(sysGroupId, processTask.getBusinessTableName(), processTask.getBusinessId(), processInstance.getPageId(), processInstance.getPageUri());
                ProcessTaskRsp task = ProcessTaskRsp.builder()
                        .procInstId(processTask.getProcInstId())
                        .taskId(processTask.getTaskId())
                        .nodeKey(processTask.getNodeKey())
                        .page(processPage)
                        .build();
                ccTasks.add(task);
            } else {
                // 组装数据
                ProcessTaskRsp task = ProcessTaskRsp.builder()
                        .procInstId(processTask.getProcInstId())
                        .taskId(processTask.getTaskId())
                        .nodeKey(processTask.getNodeKey())
                        .build();
                ccTasks.add(task);
            }
        }
        log.info("=========================> 处理抄送的节点 结果：{}", JsonUtils.toJson(ccTasks));
        return ccTasks;
    }

    /**
     * 查询页面信息
     *
     * @param sysGroupId
     * @param businessTableName
     * @param businessId
     * @param pageId
     * @param pageUri
     * @return
     */
    private ProcessPageRsp getProcessPage(String sysGroupId, String businessTableName, String businessId, String pageId, String pageUri) {
        log.info("=========================> 查询页面信息 入参：sysGroupId：{}, businessTableName: {}, businessId: {}, pageId: {}, pageUri: {}", sysGroupId, businessTableName, businessId, pageId, pageUri);
        ProcessPageRsp pageRsp;
        if (StringUtils.isBlank(pageId)) {
            pageRsp = ProcessPageRsp.builder()
                    .pageFlag(Boolean.FALSE)
                    .pageUri(pageUri)
                    .build();
        } else {
            GeneratorPageRsp tablePage = generatorPageRestApi.findByRoute(pageId).getData();
            log.info("=========================> 查询页面信息 页面信息: {}", JsonUtils.toJson(tablePage));

            // 业务数据
            GeneratorGroupRsp groupRsp = generatorGroupRestApi.findBySysGroupId(sysGroupId).getData();
            log.info("=========================> 查询页面信息 工作组信息: {}", JsonUtils.toJson(groupRsp));
            List<GeneratorCommonRsp> data = new ArrayList<>();
            if (StringUtils.isNotBlank(businessTableName) && StringUtils.isNotBlank(businessId)) {
                data = generatorCommonRestApi.findById(groupRsp.getId(), businessTableName, businessId).getList();
                log.info("=========================> 查询页面信息 业务数据: {}", JsonUtils.toJson(data));
            }

            pageRsp = ProcessPageRsp.builder()
                    .pageFlag(Objects.nonNull(tablePage))
                    .pageId(Objects.nonNull(tablePage) ? tablePage.getId() : null)
                    .pageUri(tablePage.getPageUri())
                    .pageDesign(Objects.nonNull(tablePage) ? tablePage.getPageDesign() : null)
                    .data(data)
                    .build();
        }
        log.info("=========================> 查询页面信息 结果: {}", JsonUtils.toJson(pageRsp));
        return pageRsp;
    }

    /**
     * 根据流程id查询流程集合
     *
     * @param procInstIds
     * @return
     */
    public List<ProcessInstance> findByProcInstIdIn(List<String> procInstIds) {
        if (CollectionUtils.isEmpty(procInstIds)) {
            return Lists.newArrayList();
        }
        return repository.findByProcInstIdInAndDeletedIsFalse(procInstIds);
    }

    /**
     * 根据流程id查询流程详情
     *
     * @param procInstId
     * @return
     */
    public ProcessInstance findByProcInstId(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return new ProcessInstance();
        }
        return repository.findByProcInstIdAndDeletedIsFalse(procInstId);
    }

    /**
     * 查询流程key下启用的流程正在运行的流程实例
     *
     * @param procDefKey
     * @return
     */
    public List<ProcessInstanceRsp> findRunningProcessInstance(String procDefKey) {
        if (StringUtils.isBlank(procDefKey)) {
            return new ArrayList<>();
        }
        ProcessDefinition upDef = processDefinitionService.findUpDefinitionByKey(procDefKey);
        if (Objects.isNull(upDef)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        List<ProcessInstance> processInstances = repository.findByProcDefIdAndStatusAndDeletedIsFalse(upDef.getProcDefId(), ProcessStatusEnum.PENDING.getCode());
        return MagusUtils.copyList(processInstances, ProcessInstanceRsp.class);
    }

    /**
     * 查询流程主数据
     *
     * @param businessId 业务表ID
     * @return list
     */
    public ProcessInstanceRsp findByBusinessId(String businessId) {
        if (StringUtils.isBlank(businessId)) {
            return null;
        }
        ProcessInstance instance = repository.findByBusinessIdAndDeletedIsFalse(businessId);
        ProcessInstanceRsp result = MagusUtils.copyProperties(instance, ProcessInstanceRsp.class);

        // 附件信息
        List<ProcessInstanceAppendixRt> processInstanceAppendixRts = processInstanceAppendixRtService.findByLocalProcInstId(instance.getId());
        List<ProcessAppendixRsp> appendixRsps = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processInstanceAppendixRts)) {
            List<String> appendixIds = processInstanceAppendixRts.stream().map(ProcessInstanceAppendixRt::getAppendixId).distinct().collect(Collectors.toList());
            List<AppendixRsp> appendices = appendixRestApi.list(appendixIds).getList();
            appendixRsps = MagusUtils.copyList(appendices, ProcessAppendixRsp.class);
        }
        result.setAppendices(appendixRsps);
        return result;
    }

    /**
     * 查询流程主数据
     *
     * @param businessIds 业务表ID
     * @return list
     */
    public List<ProcessInstanceRsp> listInBusinessId(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Lists.newArrayList();
        }
        List<ProcessInstance> instances = repository.findByBusinessIdInAndDeletedIsFalse(businessIds);
        return MagusUtils.copyList(instances, ProcessInstanceRsp.class);
    }

    /**
     * 查询运行过的实例
     *
     * @param procDefIds 流程定义ID
     * @return list
     */
    public List<ProcessInstanceRsp> findByProcDefIdIn(List<String> procDefIds) {
        if (CollectionUtils.isEmpty(procDefIds)) {
            return Lists.newArrayList();
        }
        return MagusUtils.copyList(repository.findByProcDefIdInAndDeletedIsFalse(procDefIds), ProcessInstanceRsp.class);
    }

    /**
     * 提交
     *
     * @param req
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_SUBMIT)
    public ProcessInstance submit(ProcessApplyReq req) {
        log.info("=========================> 提交 入参: {}", JsonUtils.toJson(req));
        if (Objects.isNull(req)) {
            return new ProcessInstance();
        }
        ProcessInstance pendingInst = repository.findByIdAndStatusAndDeletedIsFalse(req.getId(), ProcessStatusEnum.PENDING.getCode());
        if (Objects.nonNull(pendingInst)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_INSTANCE_IS_PENDING);
        }
        Map<String, Object> variables = new HashMap<>();
        genTableVariables(req.getData(), variables);
        // 重启/开启流程实例
        if (BooleanUtils.isTrue(req.getExistFlag())) {
            // 编辑草稿后提交，本地存在数据
            return startInstance(update(req), variables);
        } else {
            // 申请页面直接提交，本地未生成数据
            return startInstance(saveDraft(req), variables);
        }
    }

    /**
     * 提交
     *
     * @param id
     * @param variables
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_SUBMIT)
    public ProcessInstance submit(String id, Map<String, Object> variables) {
        log.info("=========================> 提交 入参: id: {}, variables: {}", id, JsonUtils.toJson(variables));
        if (StringUtils.isBlank(id)) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        ProcessInstance instance = processInstanceService.findById(id);

        if (Objects.isNull(instance)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
        }
        if (ProcessStatusEnum.PENDING.getCode().equals(instance.getStatus())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_INSTANCE_IS_PENDING);
        }
        return startInstance(instance, variables);
    }

    /**
     * 启动流程实例
     *
     * @param localInstance
     * @param customVariables
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessInstance startInstance(ProcessInstance localInstance, Map<String, Object> customVariables) {
        log.info("=========================> 启动流程实例 入参: localInstance: {}, customVariables: {}", JsonUtils.toJson(localInstance), JsonUtils.toJson(customVariables));
        if (Objects.isNull(localInstance)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
        }
        String procInstId = localInstance.getProcInstId();
        String status = localInstance.getStatus();
        ProcessDefinitionRsp procDef = processDefinitionService.findByProcDefId(localInstance.getProcDefId());
        if (ProcessStatusEnum.UNCOMMITTED.getCode().equals(localInstance.getStatus())) {
            // 草稿状态

            // 获取流程扩展变量
            VariableMap variables = processExtService.handleAllUserTaskExtVariables(localInstance.getProcDefId());
            log.info("=========================> 启动流程实例 节点处理人/组: {}", JsonUtils.toJson(variables));
            variables.putAll(customVariables);
            log.info("=========================> 启动流程实例 流程：{} 所有的扩展变量： {}", localInstance.getId(), variables);

            org.camunda.bpm.engine.runtime.ProcessInstance instance = this.runtimeService.startProcessInstanceById(localInstance.getProcDefId(), localInstance.getId(), variables);
            log.info("=========================> 启动流程实例 流程启动: {}", instance.getProcessInstanceId());
            procInstId = instance.getProcessInstanceId();
            localInstance.setStartTime(new Date());
        }
        localInstance.setProcInstId(procInstId);
        localInstance.setStatus(ProcessStatusEnum.PENDING.getCode());

        // 如果是被驳回，等处理完申请人后处理驳回逻辑
        if (ProcessStatusEnum.REJECT.getCode().equals(status)) {
            handleRejectRule(procDef, localInstance);
            localInstance.setRejectNodeKey(null);
        }

        // 保存数据至本地
        log.info("=========================> 启动流程实例 更新本地流程 {}", JsonUtils.toJson(localInstance));
        processInstanceService.save(localInstance);

        // 流程第一个节点是申请人，直接完成
        handleFirstUserTask(localInstance);

        return localInstance;
    }

    /**
     * 申请人节点优先处理
     *
     * @param processInstance
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    private void handleFirstUserTask(ProcessInstance processInstance) {
        log.info("=========================> 申请人节点处理 入参： {}", JsonUtils.toJson(processInstance));
        // 流程第一个节点是申请人，直接完成
        ProcessNode firstUserTask = processNodeService.getFirstUserTask(processInstance.getProcDefId());
        if (Objects.isNull(firstUserTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcInstId()).taskDefinitionKey(firstUserTask.getNodeKey()).singleResult();
        if (Objects.isNull(task)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_TASK_NOT_FOUND);
        }
        log.info("=========================> 申请人节点处理 任务： {}", task.getId());
        if (!StringUtils.equals(processInstance.getStartUserId(), task.getAssignee())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
        }
        taskService.complete(task.getId());

        ProcessTask firstTask = processTaskService.findByTaskId(task.getId());
        if (Objects.isNull(firstTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_TASK_NOT_FOUND);
        }
        firstTask.setBusinessTableName(processInstance.getBusinessTableName());
        firstTask.setBusinessId(processInstance.getBusinessId());
        log.info("=========================> 申请人节点处理 本地任务保存： {}", JsonUtils.toJson(firstTask));
        processTaskService.save(firstTask);

        // 下一个节点是否存在委托
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcInstId()).active().taskAssigned().list();
        tasks.forEach(targetTask -> processDelegateService.delegate(targetTask));
    }

    /**
     * 根据拒绝规则处理重新申请后的节点位置
     *
     * @param procDef
     * @param procInst
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    private void handleRejectRule(ProcessDefinitionRsp procDef, ProcessInstance procInst) {
        log.info("=========================> 根据拒绝规则处理重新申请后的节点位置 入参：流程定义： {}, 流程实例：{}", JsonUtils.toJson(procDef), JsonUtils.toJson(procInst));
        if (StringUtils.isBlank(procDef.getRejectRule())) {
            return;
        }
        ProcessNode firstUserTask = processNodeService.getFirstUserTask(procDef.getProcDefId());
        if (Objects.isNull(firstUserTask)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
        Task task = taskService.createTaskQuery().processInstanceId(procInst.getProcInstId()).taskDefinitionKey(firstUserTask.getNodeKey()).singleResult();
        if (Objects.nonNull(task)) {
            return;
        }

        switch (ProcessRejectRuleEnum.valueOf(procDef.getRejectRule())) {
            case FROM_START_NODE -> {
                // 从头开始审批
                log.info("=========================> 根据拒绝规则处理重新申请后的节点位置 默认从头开始审批，不处理");
            }
            case FROM_REJECT_NODE -> {
                log.info("=========================> 根据拒绝规则处理重新申请后的节点位置 从驳回节点审批");
                ProcessInstanceModificationBuilder builder = runtimeService.createProcessInstanceModification(procInst.getProcInstId());

                // 取消节点定义的所有任务
                Set<String> activityIdSet = new HashSet<>();
                taskService.createTaskQuery().processInstanceId(procInst.getProcInstId()).active().list().forEach(taskQuery -> {
                    String activityId = taskQuery.getTaskDefinitionKey();
                    if (activityIdSet.add(activityId)) {
                        builder.cancelAllForActivity(activityId);
                    }
                });
                // 跳转从驳回节点审批
                ProcessNodeRsp targetNode = processNodeService.findByProcDefIdAndNodeKey(task.getProcessDefinitionId(), procInst.getRejectNodeKey());
                if (StringUtils.equals(ActivityTypes.MULTI_INSTANCE_BODY, targetNode.getNodeType())) {
                    VariableMap variables = processExtService.getExtVariablesByNode(targetNode.getProcDefId(), targetNode.getNodeKey());
                    List<String> assignees = (List<String>) variables.get(procInst.getRejectNodeKey() + ProcessExtService.ASSIGNEE_LIST);
                    if (CollectionUtils.isNotEmpty(assignees)) {
                        for (String assignee : assignees) {
                            builder.startBeforeActivity(procInst.getRejectNodeKey())
                                    .setVariable(procInst.getRejectNodeKey() + "_assignee", assignee);
                        }
                    }
                    List<String> groups = (List<String>) variables.get(procInst.getRejectNodeKey() + ProcessExtService.CANDIDATE_GROUP);
                    if (CollectionUtils.isNotEmpty(groups)) {
                        List<UserRsp> users = userRestApi.listAssignee(groups).getList();
                        for (UserRsp user : users) {
                            builder.startBeforeActivity(procInst.getRejectNodeKey())
                                    .setVariable(procInst.getRejectNodeKey() + "_assignee", user.getLoginId());
                        }
                    }
                } else {
                    builder.startBeforeActivity(procInst.getRejectNodeKey());
                }
                builder.execute();
            }
            default -> {
                log.warn(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST.getMessage());
            }
        }
    }

    /**
     * 递归获取所有一条元数据的值作为全局变量
     *
     * @param metaData
     * @param variables
     */
    public void genTableVariables(List<GeneratorCommonReq> metaData, Map<String, Object> variables) {
        if (CollectionUtils.isEmpty(metaData)) {
            return;
        }
        for (GeneratorCommonReq data : metaData) {
            String tableName = data.getTableKey();
            List<Map<String, Object>> tableData = data.getTableData();
            List<GeneratorCommonReq> subForms = data.getSubForms();
            if (CollectionUtils.isNotEmpty(tableData) && tableData.size() == 1) {
                for (Map.Entry<String, Object> entry : tableData.get(0).entrySet()) {
                    if (entry == null) {
                        continue;
                    }
                    // 例： key : tableName_column  value: value
                    variables.put(tableName + "_" + entry.getKey(), entry.getValue());
                }
            }
            if (CollectionUtils.isNotEmpty(subForms)) {
                genTableVariables(subForms, variables);
            }
        }
    }

    /**
     * 保存草稿
     *
     * @param req
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessInstance saveDraft(ProcessApplyReq req) {
        log.info("=========================> 流程保存草稿 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req)) {
            return new ProcessInstance();
        }

        // 查询流程信息
        ProcessDefinitionRsp processDefinition = this.processDefinitionService.findByProcDefId(req.getProcDefId());
        if (Objects.isNull(processDefinition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }

        AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(processDefinition.getProcDefKey()).getData();
        if (Objects.isNull(sysGroup)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }

        // 查询是否是web版功能组
        GeneratorGroupRsp group = this.generatorGroupRestApi.findBySysGroupId(sysGroup.getId()).getData();

        String table = req.getBusinessTableName();
        String primaryInfo = req.getBusinessDataId();
        if (Objects.isNull(group)) {
            log.warn(ProcessResultEnum.RESULT_ERROR_GROUP_NOT_EXIST.getMessage());
        } else {
            // 存在web版功能组，执行SQL数据，保存业务数据
            if (CollectionUtils.isNotEmpty(req.getData())) {
                List<GeneratorCommonUpsertRsp> upsert = generatorCommonRestApi.create(group.getId(), req.getData()).getList();
                if (CollectionUtils.isEmpty(upsert)) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_DATA_NOT_EXIST);
                }
                GeneratorCommonUpsertRsp generatorCommonUpsertRsp = upsert.get(0);
                table = generatorCommonUpsertRsp.getTable();
                primaryInfo = generatorCommonUpsertRsp.getPrimaryInfo().getValue();
                log.info("=========================> 流程保存草稿 功能组业务数据结果：表名：{}, 主键：{}", table, primaryInfo);
            }
        }

        // 暂存数据
        ProcessInstance instance = saveProcessInstance(sysGroup, processDefinition.getProcDefId(), table, primaryInfo, req.getPageId(), req.getPageUri());

        // 保存附件实例关系
        processInstanceAppendixRtService.batchSaveAppendicesRt(instance.getId(), req.getAppendixIds());

        return instance;
    }

    /**
     * 模拟测试保存草稿（简单保存）
     *
     * @param procDefId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessInstance mockSaveDraft(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            return new ProcessInstance();
        }

        // 暂存数据
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcDefId(procDefId);
        processInstance.setStartUserId(LoginUserUtils.getLoginId());
        processInstance.setStatus(ProcessStatusEnum.UNCOMMITTED.getCode());

        UserRsp user = userRestApi.findByLoginId(LoginUserUtils.getLoginId()).getData();
        processInstance.setStartUserName(Objects.nonNull(user) ? user.getUserName() : null);

        ProcessInstance instance = processInstanceService.save(processInstance);

        return instance;
    }

    /**
     * 暂存数据
     *
     * @param group
     * @param procDefId
     * @param businessTableName
     * @param businessId
     * @param pageId            TODO whale 待删
     * @param pageUri           TODO whale 待删
     * @return
     */
    private ProcessInstance saveProcessInstance(AppGroupRsp group, String procDefId, String businessTableName, String businessId, String pageId, String pageUri) {
        log.info("=========================> 暂存数据 入参：group：{}, procDefId:{}, businessTableName:{}, businessId: {}, pageId: {}, pageUri:{}",
                JsonUtils.toJson(group), procDefId, businessTableName, businessId, pageId, pageUri);
        if (Objects.isNull(group) || StringUtils.isBlank(procDefId)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_DATA_NOT_EXIST);
        }

        ProcessInstance exist = repository.findByBusinessTableNameAndBusinessIdAndDeletedIsFalse(businessTableName, businessId);
        if (Objects.nonNull(exist)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BUSINESS_DATA_EXIST);
        }

        String now = DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now());
        String processNo = group.getCode() + "-" + now + "-" + LocalDateTime.now().getNano();
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcessNo(processNo);
        processInstance.setTitle(LoginUserUtils.getUserName() + "-" + group.getName() + "-" + now);
        processInstance.setProcDefId(procDefId);
        processInstance.setStartUserId(LoginUserUtils.getLoginId());
        processInstance.setStatus(ProcessStatusEnum.UNCOMMITTED.getCode());
        processInstance.setPageId(pageId);
        processInstance.setPageUri(pageUri);
        // 存在业务数据，保存业务数据
        if (StringUtils.isNotBlank(businessTableName) || StringUtils.isNotBlank(businessId)) {
            GeneratorTableRsp table = generatorTableRestApi.findByName(businessTableName).getData();
            processInstance.setBusinessId(businessId);
            processInstance.setBusinessTableName(Objects.nonNull(table) ? table.getTableName() : null);
        }

        UserRsp user = userRestApi.findByLoginId(LoginUserUtils.getLoginId()).getData();
        processInstance.setStartUserName(Objects.nonNull(user) ? user.getUserName() : null);

        ProcessInstance instance = processInstanceService.save(processInstance);
        return instance;
    }

    /**
     * 流程更新
     *
     * @param req
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessInstance update(ProcessApplyReq req) {
        log.info("=========================> 流程更新 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req) || StringUtils.isBlank(req.getId())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }

        // 查询流程实例
        ProcessInstance instance = processInstanceService.findById(req.getId());
        if (Objects.isNull(instance)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
        }
        if (ProcessStatusEnum.PENDING.getCode().equals(instance.getStatus())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_INSTANCE_IS_PENDING);
        }
        if (!StringUtils.equals(req.getPageId(), instance.getPageId()) || !StringUtils.equals(req.getPageUri(), instance.getPageUri())) {
            instance.setPageId(req.getPageId());
            instance.setPageUri(req.getPageUri());
            repository.save(instance);
        }

        // 查询流程信息
        ProcessDefinitionRsp definition = this.processDefinitionService.findByProcDefId(instance.getProcDefId());
        if (Objects.isNull(definition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }

        // 编辑文件上传
        List<String> appendixIds = req.getAppendixIds();
        log.info("=========================> 流程更新 处理附件：{}", JsonUtils.toJson(appendixIds));
        // 获取已上传的文件
        List<ProcessInstanceAppendixRt> appendixRts = processInstanceAppendixRtService.findByLocalProcInstId(req.getId());
        if (CollectionUtils.isEmpty(appendixIds) && CollectionUtils.isNotEmpty(appendixRts)) {
            log.info("=========================> 流程更新 附件删除：{}", JsonUtils.toJson(appendixRts));
            // 删除本地
            deleteProcInstAppendix(appendixRts);
        } else if (CollectionUtils.isNotEmpty(appendixIds) && CollectionUtils.isEmpty(appendixRts)) {
            log.info("=========================> 流程更新 附件保存：{}", JsonUtils.toJson(appendixRts));
            // 保存
            processInstanceAppendixRtService.batchSaveAppendicesRt(instance.getId(), req.getAppendixIds());
        } else if (CollectionUtils.isNotEmpty(appendixIds) && CollectionUtils.isNotEmpty(appendixRts)) {
            log.info("=========================> 流程更新 附件先删除后保存：{}", JsonUtils.toJson(appendixRts));
            List<String> localAppendixIds = appendixRts.stream().map(ProcessInstanceAppendixRt::getAppendixId).collect(toList());
            localAppendixIds.removeAll(appendixIds);
            if (CollectionUtils.isNotEmpty(localAppendixIds)) {
                // 先删除
                deleteProcInstAppendix(appendixRts);
                // 在保存
                processInstanceAppendixRtService.batchSaveAppendicesRt(instance.getId(), req.getAppendixIds());
            }
        }

        // 查询web功能组信息
        AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(definition.getProcDefKey()).getData();
        if (Objects.isNull(sysGroup)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }
        GeneratorGroupRsp group = this.generatorGroupRestApi.findBySysGroupId(sysGroup.getId()).getData();
        if (Objects.isNull(group)) {
            log.warn(ProcessResultEnum.RESULT_ERROR_GROUP_NOT_EXIST.getMessage());
        } else {
            // 编辑元数据
            List<GeneratorCommonReq> datas = req.getData();
            if (CollectionUtils.isNotEmpty(datas)) {
                log.info("=========================> 流程更新 更新元数据：{}", JsonUtils.toJson(datas));
                // 更新元数据
                generatorCommonRestApi.update(group.getId(), datas);
            }
        }

        return instance;
    }


    /**
     * 流程删除（支持草稿和被驳回的流程删除）
     *
     * @param id
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delete(String id) {
        log.info("=========================> 流程删除 入参：{}", id);
        if (StringUtils.isBlank(id)) {
            return;
        }
        // 删除本地数据
        ProcessInstance processInstance = processInstanceService.findById(id);
        if (Objects.isNull(processInstance)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_NOT_FOUND);
        }

        if (!StringUtils.equals(processInstance.getCreateId(), LoginUserUtils.getLoginId())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
        }
        processInstance.setDeleted(Boolean.TRUE);
        processInstanceService.save(processInstance);

        if (ProcessStatusEnum.REJECT.getCode().equals(processInstance.getStatus())) {
            log.info("=========================> 流程删除 删除camunda流程：{}", processInstance.getProcInstId());
            // 存在启动流程，删除camunda流程
            runtimeService.deleteProcessInstance(processInstance.getProcInstId(), "被驳回流程删除");
        }

        // 流程与功能组之间的关系
        ProcessDefinitionRsp procDef = processDefinitionService.findByProcDefId(processInstance.getProcDefId());
        AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(procDef.getProcDefKey()).getData();
        if (Objects.isNull(sysGroup)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
        }
        GeneratorGroupRsp group = generatorGroupRestApi.findBySysGroupId(sysGroup.getId()).getData();
        if (Objects.isNull(group)) {
            return;
        }

        List<ProcessTask> nodeInstances = processTaskService.findByProcInstId(processInstance.getProcInstId());
        if (CollectionUtils.isNotEmpty(nodeInstances)) {
            // 驳回的场景，已经产生数据，需要删除节点对应的数据
            for (ProcessTask nodeInstance : nodeInstances) {
                if (StringUtils.isBlank(nodeInstance.getBusinessTableName()) || StringUtils.isBlank(nodeInstance.getBusinessId())) {
                    continue;
                }
                log.info("=========================> 流程删除 删除节点业务表数据：{}, 表名：{}, 业务数据：{}", group.getId(), nodeInstance.getBusinessTableName(), nodeInstance.getBusinessId());
                // 删除节点业务表数据
                generatorCommonRestApi.delete(group.getId(), nodeInstance.getBusinessTableName(), Arrays.asList(nodeInstance.getBusinessId()));
            }
        }

        // 删除流程业务表数据
        log.info("=========================> 流程删除 删除流程业务表数据：{}, 表名：{}, 业务数据：{}", group.getId(), processInstance.getBusinessTableName(), processInstance.getBusinessId());
        generatorCommonRestApi.delete(group.getId(), processInstance.getBusinessTableName(), Arrays.asList(processInstance.getBusinessId()));
    }


    /**
     * 删除业务表数据对应的流程数据
     *
     * @param businessIds 业务表ID集合
     * @return list
     */
    public void deleteByBusinessIdIn(List<String> businessIds) {
        List<ProcessInstance> list = repository.findByBusinessIdInAndDeletedIsFalse(businessIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // procInstId为空时，说明流程未启动，可以删除
        List<ProcessInstance> filterList = list.stream().filter(instance -> StringUtils.isNotBlank(instance.getProcInstId())).toList();
        if (CollectionUtils.isNotEmpty(filterList)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_EXIST);
        }
        this.deleteAll(list);
    }

    /**
     * 取消附件和流程的关系
     *
     * @param appendixRts
     */
    private void deleteProcInstAppendix(List<ProcessInstanceAppendixRt> appendixRts) {
        appendixRts.forEach(x -> {
            x.setDeleted(Boolean.FALSE);
        });
        processInstanceAppendixRtService.saveAll(appendixRts);

        List<String> appendixIds = appendixRts.stream().map(ProcessInstanceAppendixRt::getAppendixId).collect(toList());

        appendixRestApi.batchDelete(appendixIds);
    }
}
