package com.whale.framework.process.service;

import camundafeel.de.odysseus.el.ExpressionFactoryImpl;
import camundafeel.de.odysseus.el.util.SimpleContext;
import camundafeel.javax.el.ExpressionFactory;
import camundafeel.javax.el.ValueExpression;
import com.google.common.collect.Lists;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.constants.OriginalSqlConstant;
import com.whale.framework.process.dto.ProcessHistoricInstanceDTO;
import com.whale.framework.process.dto.ProcessIdentityLinkDTO;
import com.whale.framework.process.entity.ProcessNodeAction;
import com.whale.framework.process.entity.ProcessTask;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessAssigneeRelationEnum;
import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import com.whale.framework.process.factory.ProcessActionStrategyFactory;
import com.whale.framework.process.repository.ProcessTaskRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.datasource.DynamicDataSourceUtil;
import com.magus.framework.datasource.JdbcTemplateActuator;
import com.magus.framework.service.BaseService;
import com.magus.framework.system.api.DictRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.WorkgroupRestApi;
import com.magus.framework.system.api.dto.rsp.EntryRsp;
import com.magus.framework.system.api.dto.rsp.OrgDutyUserRsp;
import com.magus.framework.system.api.dto.rsp.UserRsp;
import com.magus.framework.system.api.dto.rsp.WorkgroupUserRestRsp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2023/5/15 2:42 PM
 */
@Service
@Slf4j
public class ProcessTaskService extends BaseService<ProcessTask, String> {

    @Autowired
    ProcessTaskRepository repository;

    @Autowired
    TaskService taskService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ProcessNodeActionService processNodeActionService;

    @Autowired
    WorkgroupRestApi workgroupRestApi;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    DictRestApi dictRestApi;

    @Autowired
    ProcessActionStrategyFactory processActionStrategyFactory;

    @Autowired
    JdbcTemplateActuator jdbcTemplate;

    /**
     * 根据流程任务ID查询
     *
     * @param taskId
     * @return
     */
    public ProcessTask findByTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return null;
        }
        return repository.findByTaskIdAndDeletedIsFalse(taskId);
    }

    /**
     * 根据流程实例ID查询
     *
     * @param procInstId
     * @return
     */
    public List<ProcessTask> findByProcInstId(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return null;
        }
        return repository.findByProcInstIdAndDeletedIsFalse(procInstId);
    }

    /**
     * 根据流程实例ID和节点key查询
     *
     * @param procInstId
     * @return
     */
    public List<ProcessTask> findByProcInstIdAndNodeKey(String procInstId, String nodeKey) {
        if (StringUtils.isBlank(procInstId) || StringUtils.isBlank(nodeKey)) {
            return new ArrayList<>();
        }
        return repository.findByProcInstIdAndNodeKeyAndDeletedIsFalseOrderByCreateTime(procInstId, nodeKey);
    }

    public List<ProcessTask> listTaskByTaskId(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        return repository.findByTaskIdInAndDeletedIsFalse(taskIds);
    }

    /**
     * 获取当前任务
     *
     * @param procInstId
     * @return
     */
    public ProcessHistoricInstanceDTO getCurrentTask(String procInstId) {
        log.info("获取当前任务 入参：{}", procInstId);
        ProcessHistoricInstanceDTO result = new ProcessHistoricInstanceDTO();
        Task task = taskService.newTask();
        Boolean claimFlag = Boolean.FALSE;
        if (StringUtils.isBlank(procInstId)) {
            return new ProcessHistoricInstanceDTO();
        }
        // 获取当前流程实例所执行到的节点位置
        List<Task> assigneeTasks = taskService.createTaskQuery().taskAssigned().taskAssignee(LoginUserUtils.getLoginId()).processInstanceId(procInstId).list();

        // 优先人处理
        if (CollectionUtils.isNotEmpty(assigneeTasks)) {
            // 如果该节点同时节点实例分配了多个人，均获取第一人执行任务
            task = assigneeTasks.get(0);
        }
        // 没有指定人，即获取工作组
        if (Objects.isNull(task) || StringUtils.isBlank(task.getId())) {
            // 获取当前用户所归属的组织信息
            List<String> ruTaskGroupIds = getRuTaskGroupId(LoginUserUtils.getLoginId());
            // 获取正在运行的组任务
            List<ProcessIdentityLinkDTO> links = listGroupTaskByGroupId(ruTaskGroupIds);
            List<String> taskIds = links.stream().map(ProcessIdentityLinkDTO::getTaskId).collect(toList());

            List<Task> groupTasks = taskService.createTaskQuery()
                    .processInstanceId(procInstId)
                    .taskIdIn(taskIds.toArray(new String[taskIds.size()]))
                    .list();
            if (CollectionUtils.isNotEmpty(groupTasks)) {
                // 存在组任务
                task = groupTasks.get(0);
                claimFlag = Boolean.TRUE;
            }
        }

        result.setTaskId(task.getId());
        result.setProcInstId(task.getProcessInstanceId());
        result.setNodeKey(task.getTaskDefinitionKey());
        result.setClaimFlag(claimFlag);
        log.info("获取当前任务 结果：{}", JsonUtils.toJson(result));
        return result;
    }


    /**
     * 获取用户运行中的代理组
     *
     * @param loginId
     * @return
     */
    public List<String> getRuTaskGroupId(String loginId) {
        List<String> groupIds = new ArrayList<>();
        UserRsp userRsp = userRestApi.findByLoginId(loginId).getData();

        // 1.1 处理工作组uuid
        List<WorkgroupUserRestRsp> workGroups = workgroupRestApi.findByUserId(userRsp.getId()).getList();
        if (CollectionUtils.isNotEmpty(workGroups)) {
            log.info("=========================> 获取用户所属工作组：{}", JsonUtils.toJson(workGroups));
            List<String> workgroupIds = workGroups.stream().map(WorkgroupUserRestRsp::getWorkgroupId).collect(Collectors.toList());
            // 拼接成特定的uuid
            groupIds.addAll(workgroupIds.stream().map(x -> ProcessAssigneeTypeEnum.$WORKGROUP.getPre() + x).collect(toList()));
        }

        List<OrgDutyUserRsp> orgDutyUsers = userRestApi.listOrgDutyUser(null, null, userRsp.getId()).getList();
        if (CollectionUtils.isNotEmpty(orgDutyUsers)) {
            log.info("=========================> 获取用户所属组织职务：{}", JsonUtils.toJson(orgDutyUsers));
            for (OrgDutyUserRsp orgDutyUser : orgDutyUsers) {
                String orgId = orgDutyUser.getOrgId();
                String dutyId = orgDutyUser.getDutyId();

                String orgUuid = ProcessAssigneeTypeEnum.$ORG.getPre() + orgId;
                groupIds.add(orgUuid);

                if (StringUtils.isNotBlank(dutyId)) {
                    // 1.2 处理职务uuid
                    String dutyUuid = ProcessAssigneeTypeEnum.$DUTY.getPre() + dutyId;
                    groupIds.add(dutyUuid);

                    // 1.3 处理组织和职务uuid
                    String orgDutyUuid = ProcessAssigneeTypeEnum.$ORG.getPre() + orgId + "@" + dutyId;
                    groupIds.add(orgDutyUuid);
                }
            }
        }
        log.info("=========================> 获取用户所属代理组信息：{}", JsonUtils.toJson(groupIds));
        return groupIds;
    }

    /**
     * 根据任务获取运行中的组任务
     *
     * @param taskIds
     * @return
     */
    public List<ProcessIdentityLinkDTO> listGroupTaskByTaskId(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String ids = taskIds.stream().distinct().map(str -> "'" + str + "'").collect(joining(" , "));
        String executeSql = String.format(OriginalSqlConstant.SELECT_ACT_RU_IDENTITYLINK_3, ids);
        List<ProcessIdentityLinkDTO> links = executeIdentityLink(executeSql);
        return links;
    }

    /**
     * 根据功能组获取运行中的组任务
     *
     * @param groupIds
     * @return
     */
    public List<ProcessIdentityLinkDTO> listGroupTaskByGroupId(List<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        String ids = groupIds.stream().distinct().map(str -> "'" + str + "'").collect(joining(" , "));
        String executeSql = String.format(OriginalSqlConstant.SELECT_ACT_RU_IDENTITYLINK_1, ids);
        List<ProcessIdentityLinkDTO> links = executeIdentityLink(executeSql);
        log.info("=========================> 根据代理组id获取代理组信息：{}", JsonUtils.toJson(links));
        return links;
    }

    /**
     * 执行 ACT_RU_IDENTITYLINK 表的查询并转换结果
     *
     * @param executeSql
     * @return
     */
    private List<ProcessIdentityLinkDTO> executeIdentityLink(String executeSql) {
        List<Map<String, Object>> objs = jdbcTemplate.queryForListToDb(executeSql, DynamicDataSourceUtil.getDbName());

        List<ProcessIdentityLinkDTO> links = objs.stream().map(map -> {
            ProcessIdentityLinkDTO dataRsp = new ProcessIdentityLinkDTO();
            dataRsp.setId(Objects.toString(map.get("ID_"), null));
            dataRsp.setRev(Objects.toString(map.get("REV_"), null));
            dataRsp.setGroupId(Objects.toString(map.get("GROUP_ID_"), null));
            dataRsp.setType(Objects.toString(map.get("TYPE_"), null));
            dataRsp.setTaskId(Objects.toString(map.get("TASK_ID_"), null));
            dataRsp.setUserId(Objects.toString(map.get("USER_ID_"), null));
            dataRsp.setProcDefId(Objects.toString(map.get("PROC_DEF_ID_"), null));
            dataRsp.setTenantId(Objects.toString(map.get("TENANT_ID_"), null));
            return dataRsp;
        }).collect(toList());
        return links;
    }

    /**
     * 获取当前运行的节点
     *
     * @param procInstId
     * @return
     */
    public List<ProcessNodeRsp> findActivityInstance(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return new ArrayList<>();
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();

        List<ProcessNodeRsp> nodes = tasks.stream().map(task -> {
            ProcessNodeRsp rsp = new ProcessNodeRsp();
            rsp.setNodeKey(task.getTaskDefinitionKey());
            return rsp;
        }).collect(toList());
        return nodes;
    }

    /**
     * 执行操作
     *
     * @param req
     */
    public void execute(ProcessTaskActionReq req) {
        processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
    }

    /**
     * 批量审批
     *
     * @param taskIds
     */
    public void batchApprove(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {

        }
        List<ProcessTask> tasks = repository.findByTaskIdInAndDeletedIsFalse(taskIds);

        List<ProcessNodeAction> nodeActions = processNodeActionService.listByNodeKey(tasks.stream().map(ProcessTask::getNodeKey).collect(Collectors.toList()));
        if (nodeActions.stream().filter(action -> ProcessActionEnum.ACTION_BATCH_APPROVAL.getCode().equals(action.getActionCode())).collect(toList()).size() != taskIds.size()) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_UNABLE_OPERATE);
        }
        tasks.forEach(task -> {
            ProcessTaskActionReq req = new ProcessTaskActionReq();
            req.setAction(ProcessActionEnum.ACTION_COMPLETE.getCode());
            req.setProcInstId(task.getProcInstId());
            req.setTaskId(task.getTaskId());

            ProcessTaskActionReq.ProcessTerminateActionReq terminateActionReq = new ProcessTaskActionReq.ProcessTerminateActionReq();
            terminateActionReq.setOpinion(ProcessActionEnum.ACTION_COMPLETE.getDesc());
            req.setTerminate(terminateActionReq);

            processActionStrategyFactory.getStrategy(req.getAction()).execute(req);
        });
    }

    /**
     * 根据任务id查询代理组和代理人
     *
     * @param taskIds
     */
    public List<String> findGroupUser(List<String> taskIds) {
        log.info("=========================> 根据任务id查询代理组和代理人 入参：{}", JsonUtils.toJson(taskIds));
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            // 1. 查询link
            List<ProcessIdentityLinkDTO> links = this.listGroupTaskByTaskId(taskIds);
            log.info("=========================> 根据任务id查询代理组和代理人 代理组：{}", JsonUtils.toJson(links));
            List<String> groupIds = links.stream().map(ProcessIdentityLinkDTO::getGroupId).collect(toList());
            // 2. 将运行中的组拼接
            if (CollectionUtils.isNotEmpty(groupIds)) {
                List<String> orgIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$ORG.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(orgIds)) {
                    for (String orgId : orgIds) {
                        String[] orgDuty = StringUtils.removeStart(orgId, ProcessAssigneeTypeEnum.$ORG.getPre()).split("@");
                        String g1 = orgDuty[0];
                        String g2 = null;
                        if (orgDuty.length > 1) {
                            g2 = orgDuty[1];
                        }
                        List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listByOrgIdAndDutyIdIn(g1, Collections.singletonList(g2)).getList();
                        List<String> userIds = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).collect(toList());
                        log.info("=========================> 根据任务id查询代理组和代理人 组织：{}", JsonUtils.toJson(userIds));
                        result.addAll(userIds);
                    }
                }
                List<String> workgroupIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$WORKGROUP.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(workgroupIds)) {
                    for (String workgroupId : workgroupIds) {
                        String[] workgroupDuty = StringUtils.removeStart(workgroupId, ProcessAssigneeTypeEnum.$WORKGROUP.getPre()).split("@");
                        String g1 = workgroupDuty[0];
                        String g2 = null;
                        if (workgroupDuty.length > 1) {
                            g2 = workgroupDuty[1];
                        }
                        List<WorkgroupUserRestRsp> workgroupRspList = workgroupRestApi.listByWorkGroupIds(Collections.singletonList(g1)).getList();
                        List<String> userIds1 = workgroupRspList.stream().map(WorkgroupUserRestRsp::getUserId).collect(toList());
                        List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listOrgByDutyIds(Collections.singletonList(g2)).getList();
                        List<String> userIds2 = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).collect(toList());

                        List<String> userIds = userIds1.stream()
                                .filter(userIds2::contains)
                                .collect(toList());

                        log.info("=========================> 根据任务id查询代理组和代理人 工作组：{}", JsonUtils.toJson(userIds));
                        result.addAll(userIds);
                    }
                }
                List<String> dutyIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$DUTY.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(dutyIds)) {
                    List<String> ids = dutyIds.stream().map(x -> StringUtils.removeStart(x, ProcessAssigneeTypeEnum.$DUTY.getPre())).collect(toList());
                    List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listOrgByDutyIds(ids).getList();
                    List<String> userIds = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).collect(toList());
                    log.info("=========================> 根据任务id查询代理组和代理人 职务：{}", JsonUtils.toJson(userIds));
                    result.addAll(userIds);
                }
                List<String> rtIds = groupIds.stream().filter(Objects::nonNull).filter(x -> x.startsWith(ProcessAssigneeTypeEnum.$RELATION.getPre())).collect(toList());
                if (CollectionUtils.isNotEmpty(rtIds)) {
                    List<String> ids = rtIds.stream().map(x -> StringUtils.removeStart(x, ProcessAssigneeTypeEnum.$RELATION.getPre())).collect(toList());
                    for (String id : ids) {
                        EntryRsp entry = dictRestApi.findEntryById(id).getData();
                        String code = entry.getCode();
                        ProcessAssigneeRelationEnum relation = ProcessAssigneeRelationEnum.findByCode(code);

                        // 多实例处理
                        List<String> userIds = handleMultiRelation(dutyIds, relation);
                        log.info("=========================> 根据任务id查询代理组和代理人 关系：{}", JsonUtils.toJson(userIds));
                        result.addAll(userIds);
                    }

                    List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listOrgByDutyIds(ids).getList();
                    List<String> userIds = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).collect(toList());
                    log.info("=========================> 根据任务id查询代理组和代理人 组织职务：{}", JsonUtils.toJson(userIds));
                    result.addAll(userIds);
                }
            }
        }
        return result;
    }

    /**
     * 多实例关系处理
     *
     * @param dutyIds
     * @param relation
     */
    private List<String> handleMultiRelation(List<String> dutyIds, ProcessAssigneeRelationEnum relation) {
        log.info("=========================> 多实例关系处理 关系：{}， relation ：{}", JsonUtils.toJson(dutyIds), relation.getRelation());
        List<String> result = new ArrayList<>();
        if (ProcessAssigneeRelationEnum.APPLICANT == relation) {
            // 申请人即启动人
            result.add(LoginUserUtils.getLoginId());
        } else if (ProcessAssigneeRelationEnum.applicantOrg(relation)) {
            if (relation.getRelation() >= 0) {
                List<UserRsp> users = userRestApi.listRelationUsers(LoginUserUtils.getLoginId(), dutyIds, relation.getRelation()).getList();
                List<String> loginIds = users.stream().map(UserRsp::getLoginId).collect(Collectors.toList());
                result.addAll(loginIds);
            } else {
                result.add(LoginUserUtils.getLoginId());
            }
        } else {
            throw new MagusException("不支持");
        }
        return result;
    }

    /**
     * 获取下一个节点任务信息 TODO 待测试
     *
     * @param procDefId
     * @param procInstId
     * @param activityId
     * @param variables
     * @return
     * @throws Exception
     */
    public List<TaskDefinition> getNextActivities(String procDefId, String procInstId, String activityId, Map<String, Object> variables) {
        try {
            List<TaskDefinition> tasks = new CopyOnWriteArrayList<>();
            // 获取流程发布Id信息
            ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(procDefId);
            // 当前流程节点Id信息
            log.info("execution----->" + activityId);
            // 获取流程所有节点信息
            List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();
            // 遍历所有节点信息
            for (ActivityImpl activityImpl : activitiList) {
                if (activityId.equals(activityImpl.getId())) {
                    // 获取下一个节点信息
                    tasks = nextTaskDefinitions(activityImpl, activityImpl.getId(), procInstId, variables);
                    break;
                }
            }

            return tasks;
        } catch (Exception e) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_NODE_NOT_EXIST);
        }
    }

    /**
     * 获取下一个节点定义
     *
     * @param activityImpl
     * @param activityId
     * @param processInstanceId
     * @param condition
     * @return
     */
    private List<TaskDefinition> nextTaskDefinitions(ActivityImpl activityImpl, String activityId, String processInstanceId, Map<String, Object> condition) {
        try {
            PvmActivity ac = null;
            Object s = null;
            log.info("activityImpl.getActivityId()---->" + activityImpl.getActivityId() + "---activityId---------->" + activityId + "--->" + activityImpl.getProperty("type"));
            // List<TaskDefinition> taskDefinitions = newArrayList<TaskDefinition>();会出现java.util.ConcurrentModificationException异常，改成CopyOnWriteArrayList
            List<TaskDefinition> taskDefinitions = new CopyOnWriteArrayList<TaskDefinition>();
            // 如果遍历节点为用户任务并且节点不是当前节点信息
            if ("userTask".equals(activityImpl.getProperty("type")) && !activityId.equals(activityImpl.getId())) {
                // 获取该节点下一个节点信息
                TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityImpl.getActivityBehavior())
                        .getTaskDefinition();
                taskDefinitions.add(taskDefinition);
            } else if (activityImpl.getProperty("type").toString().contains("EndEvent") && !activityId.equals(activityImpl.getId())) {
                // 设置结束节点
                TaskDefinition taskDefinition = new TaskDefinition(null);
                ExpressionManager expressionManager = Context
                        .getProcessEngineConfiguration()
                        .getExpressionManager();
                taskDefinition.setKey(activityImpl.getId() == null ? "end" : activityImpl.getId());
                String name = activityImpl.getProperty("name") == null ? "结束" : activityImpl.getProperty("name").toString();
                taskDefinition.setNameExpression(expressionManager.createExpression(name));
                taskDefinitions.add(taskDefinition);
            } else if ("multiInstanceBody".equals(activityImpl.getProperty("type")) && !activityId.equals(activityImpl.getId())) {
                // 获取该节点下一个节点信息
                List<ActivityImpl> list = ((ActivityImpl) activityImpl).getActivities();
                for (ActivityImpl act : list) {
                    TaskDefinition taskDefinition = ((UserTaskActivityBehavior) act.getActivityBehavior())
                            .getTaskDefinition();
                    taskDefinitions.add(taskDefinition);
                }
            } else if ("exclusiveGateway".equals(activityImpl.getProperty("type")) || "inclusiveGateway".equals(activityImpl.getProperty("type"))) {
                // 当前节点为exclusiveGateway或inclusiveGateway
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                String defaultTransition = (String) activityImpl.getProperty("default");
                if (outTransitions.size() == 1) {
                    taskDefinitions.addAll(nextTaskDefinitions((ActivityImpl) outTransitions.get(0).getDestination(),
                            activityId, processInstanceId, condition));
                } else if (outTransitions.size() > 1) {
                    // 如果排他网关有多条线路信息
                    for (PvmTransition tr1 : outTransitions) {
                        ActivityImpl actImpl = (ActivityImpl) tr1.getDestination();
                        if (actImpl.getProperty("type").toString().contains("EndEvent")) {
                            TaskDefinition taskDefinition = new TaskDefinition(null);
                            ExpressionManager expressionManager = Context
                                    .getProcessEngineConfiguration()
                                    .getExpressionManager();
                            taskDefinition.setKey(actImpl.getId() == null ? "end" : actImpl.getId());
                            String name = actImpl.getProperty("name") == null ? "结束"
                                    : actImpl.getProperty("name").toString();
                            taskDefinition.setNameExpression(expressionManager.createExpression(name));
                            taskDefinitions.add(taskDefinition);
                            break;
                        }
                        // 获取排他网关线路判断条件信息
                        s = tr1.getProperty("conditionText");
                        if (null == s) {
                            continue;
                        }
                        // 判断el表达式是否成立
                        if (isCondition(condition, StringUtils.trim(s.toString()))) {
                            taskDefinitions.addAll(nextTaskDefinitions((ActivityImpl) tr1.getDestination(), activityId,
                                    processInstanceId, condition));
                        }
                    }
                    if (taskDefinitions.size() == 0 && StringUtils.isNotBlank(defaultTransition)) {
                        for (PvmTransition tr3 : outTransitions) {
                            if (defaultTransition.equals(tr3.getId())) {
                                ActivityImpl actImpl = (ActivityImpl) tr3.getDestination();
                                if (actImpl.getProperty("type").toString().contains("EndEvent")) {
                                    TaskDefinition taskDefinition2 = new TaskDefinition(null);
                                    ExpressionManager expressionManager2 = Context
                                            .getProcessEngineConfiguration()
                                            .getExpressionManager();
                                    taskDefinition2.setKey(actImpl.getId() == null ? "end" : actImpl.getId());
                                    String name2 = actImpl.getProperty("name") == null ? "结束"
                                            : actImpl.getProperty("name").toString();
                                    taskDefinition2.setNameExpression(expressionManager2.createExpression(name2));
                                    taskDefinitions.add(taskDefinition2);
                                    break;
                                }

                                taskDefinitions.addAll(nextTaskDefinitions(actImpl,
                                        activityId, processInstanceId, condition));
                                log.info("taskDefinitions---333333333--->" + taskDefinitions.size());
                            }
                        }
                    }
                }
            } else if ("parrallelGateway".equals(activityImpl.getProperty("type"))) {
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                for (PvmTransition tr1 : outTransitions) {
                    taskDefinitions.addAll(nextTaskDefinitions((ActivityImpl) tr1.getDestination(), activityId,
                            processInstanceId, condition));
                }
            } else {
                // 获取节点所有流向线路信息
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                List<PvmTransition> outTransitionsTemp = null;
                for (PvmTransition tr : outTransitions) {
                    ac = tr.getDestination(); // 获取线路的终点节点
                    log.info("ac----------->" + ac.getId() + "------>" + ac.getProperty("type"));
                    // 如果流向线路为排他网关或包容网关
                    if ("exclusiveGateway".equals(ac.getProperty("type")) || "inclusiveGateway".equals(ac.getProperty("type"))) {
                        outTransitionsTemp = ac.getOutgoingTransitions();
                        String defaultTransition = (String) ac.getProperty("default");
                        // 如果排他网关只有一条线路信息
                        if (outTransitionsTemp.size() == 1) {
                            taskDefinitions.addAll(
                                    nextTaskDefinitions((ActivityImpl) outTransitionsTemp.get(0).getDestination(),
                                            activityId, processInstanceId, condition));
                        } else if (outTransitionsTemp.size() > 1) {
                            // 如果排他网关有多条线路信息
                            for (PvmTransition tr1 : outTransitionsTemp) {
                                ActivityImpl actImpl = (ActivityImpl) tr1.getDestination();
                                if (actImpl.getProperty("type").toString().contains("EndEvent")) {
                                    TaskDefinition taskDefinition2 = new TaskDefinition(null);
                                    ExpressionManager expressionManager2 = Context
                                            .getProcessEngineConfiguration()
                                            .getExpressionManager();
                                    taskDefinition2.setKey(actImpl.getId() == null ? "end" : actImpl.getId());
                                    String name2 = actImpl.getProperty("name") == null ? "结束"
                                            : actImpl.getProperty("name").toString();
                                    taskDefinition2.setNameExpression(expressionManager2.createExpression(name2));
                                    taskDefinitions.add(taskDefinition2);
                                    break;
                                }

                                log.info("taskDefinitions--1111---->" + taskDefinitions.size());
                                s = tr1.getProperty("conditionText"); // 获取排他网关线路判断条件信息
                                if (null == s) {
                                    continue;
                                }
                                // 判断el表达式是否成立
                                if (isCondition(condition, StringUtils.trim(s.toString()))) {
                                    taskDefinitions.addAll(nextTaskDefinitions(actImpl, activityId, processInstanceId, condition));
                                }

                                log.info("taskDefinitions---22222--->" + taskDefinitions.size());
                            }
                            if (taskDefinitions.size() == 0 && StringUtils.isNotBlank(defaultTransition)) {
                                for (PvmTransition tr3 : outTransitionsTemp) {
                                    if (defaultTransition.equals(tr3.getId())) {
                                        ActivityImpl actImpl = (ActivityImpl) tr3.getDestination();
                                        if (actImpl.getProperty("type").toString().contains("EndEvent")) {
                                            TaskDefinition taskDefinition2 = new TaskDefinition(null);
                                            ExpressionManager expressionManager2 = Context
                                                    .getProcessEngineConfiguration()
                                                    .getExpressionManager();
                                            taskDefinition2.setKey(actImpl.getId() == null ? "end" : actImpl.getId());
                                            String name2 = actImpl.getProperty("name") == null ? "结束"
                                                    : actImpl.getProperty("name").toString();
                                            taskDefinition2.setNameExpression(expressionManager2.createExpression(name2));
                                            taskDefinitions.add(taskDefinition2);
                                            break;
                                        }

                                        taskDefinitions.addAll(nextTaskDefinitions(actImpl,
                                                activityId, processInstanceId, condition));
                                        log.info("taskDefinitions---333333333--->" + taskDefinitions.size());
                                    }
                                }
                            }
                        }
                    } else if ("userTask".equals(ac.getProperty("type"))) {
                        taskDefinitions.add(((UserTaskActivityBehavior) ((ActivityImpl) ac).getActivityBehavior())
                                .getTaskDefinition());
                    } else if ("multiInstanceBody".equals(ac.getProperty("type"))) {
                        List<ActivityImpl> list = ((ActivityImpl) ac).getActivities();
                        for (ActivityImpl act : list) {
                            log.info("act-------------->" + act.getActivityBehavior().getClass().getTypeName());
                            TaskDefinition taskDefinition = ((UserTaskActivityBehavior) act.getActivityBehavior())
                                    .getTaskDefinition();
                            taskDefinitions.add(taskDefinition);
                        }
                    } else if (ac.getProperty("type").toString().contains("EndEvent")) {
                        // 设置结束节点
                        TaskDefinition taskDefinition = new TaskDefinition(null);
                        ExpressionManager expressionManager = Context
                                .getProcessEngineConfiguration()
                                .getExpressionManager();
                        taskDefinition.setKey(ac.getId() == null ? "end" : ac.getId());
                        String name = ac.getProperty("name") == null ? "结束" : ac.getProperty("name").toString();
                        taskDefinition.setNameExpression(expressionManager.createExpression(name));
                        taskDefinitions.add(taskDefinition);
                    } else if ("parrallelGateway".equals(ac.getProperty("type"))) {
                        List<PvmTransition> poutTransitions = ac.getOutgoingTransitions();
                        for (PvmTransition tr1 : poutTransitions) {
                            taskDefinitions.addAll(nextTaskDefinitions((ActivityImpl) tr1.getDestination(), activityId,
                                    processInstanceId, condition));
                        }
                    }
                }
            }
            return taskDefinitions;
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean isCondition(Map<String, Object> condition, String el) {
        try {
            ExpressionFactory factory = new ExpressionFactoryImpl();
            SimpleContext context = new SimpleContext();
            if (condition != null) {
                Iterator<Map.Entry<String, Object>> iterator = condition.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> value = iterator.next();
                    context.setVariable(value.getKey(), factory.createValueExpression(value.getValue(), String.class));
                }
            }
            ValueExpression e = factory.createValueExpression(context, el, boolean.class);
            return (Boolean) e.getValue(context);
        } catch (Exception e) {
            throw e;
        }
    }
}
