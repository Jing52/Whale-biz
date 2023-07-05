package com.whale.framework.process.service;

import com.whale.framework.process.dto.ProcessAssigneeDTO;
import com.whale.framework.process.enums.ProcessAssigneeRelationEnum;
import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.system.api.DictRestApi;
import com.magus.framework.system.api.OrganizationRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.WorkgroupRestApi;
import com.magus.framework.system.api.dto.rsp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程扩展字段
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Slf4j
@Service
public class ProcessExtService {

    private final static String ACTOR_SUBMIT_CANDIDATE = "actor_submit_candidate";

    private final static String ASSIGNEE = "_assignee";

    public final static String ASSIGNEE_LIST = "_assignee_list";

    public final static String CANDIDATE_GROUP = "_candidate_group";

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    DictRestApi dictRestApi;

    @Autowired
    WorkgroupRestApi workgroupRestApi;

    @Autowired
    OrganizationRestApi organizationRestApi;


    /**
     * 处理指定节点的特殊扩展变量
     *
     * @param procDefId
     * @return
     */
    public VariableMap getExtVariablesByNode(String procDefId, String nodeKey) {
        log.info("=========================> 处理指定节点的特殊扩展变量 入参： procDefId：{}", procDefId);
        VariableMap variables = Variables.createVariables();
        if (StringUtils.isBlank(procDefId)) {
            return variables;
        }
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(procDefId);

        log.info("=========================> 处理指定节点的特殊扩展变量 获取用户任务的扩展配置信息");
        Collection<UserTask> tasks = bpmnModelInstance.getModelElementsByType(UserTask.class);
        for (UserTask task : tasks) {
            if (StringUtils.equals(nodeKey, task.getId())) {
                // 默认处理人为null, 不设置识别不到参数报错
                variables.put(task.getId() + ASSIGNEE, null);
                variables.put(task.getId() + CANDIDATE_GROUP, new ArrayList<>());
                variables.put(task.getId() + ASSIGNEE_LIST, new ArrayList<>());

                // 如果节点相同，对节点的特殊扩展做特殊处理
                ExtensionElements extensionElements = task.getExtensionElements();
                if (Objects.nonNull(extensionElements)) {
                    List<CamundaProperties> camundaProperties = extensionElements.getElementsQuery()
                            .filterByType(CamundaProperties.class)
                            .list();
                    List<CamundaProperty> properties = new ArrayList<>();
                    camundaProperties.forEach(x -> properties.addAll(x.getCamundaProperties()));
                    // 处理property参数
                    for (CamundaProperty property : properties) {
                        String name = property.getCamundaName();
                        String value = property.getCamundaValue();
                        if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
                            continue;
                        }
                        log.info("=========================> 处理指定节点的特殊扩展变量 act_id: {}, property name: {}, value: {}", task.getId(), name, value);
                        // 处理人
                        if (StringUtils.equals(name, ACTOR_SUBMIT_CANDIDATE)) {
                            getAssigneeVariableMap(procDefId, task, value, variables);
                        }
                    }
                }
            }
        }
        return variables;
    }


    /**
     * 处理所有的特殊扩展变量
     *
     * @param procDefId
     * @return
     */
    public VariableMap handleAllUserTaskExtVariables(String procDefId) {
        log.info("=========================> 处理所有的特殊扩展变量 入参： procDefId：{}", procDefId);
        VariableMap variables = Variables.createVariables();
        if (StringUtils.isBlank(procDefId)) {
            return variables;
        }
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(procDefId);

        log.info("=========================> 处理所有的特殊扩展变量 获取用户任务的扩展配置信息");
        Collection<UserTask> tasks = bpmnModelInstance.getModelElementsByType(UserTask.class);
        for (UserTask task : tasks) {
            // 如果节点相同，对节点的特殊扩展做特殊处理
            ExtensionElements extensionElements = task.getExtensionElements();
            // 默认处理人为null, 不设置识别不到参数报错
            variables.put(task.getId() + ASSIGNEE, null);
            variables.put(task.getId() + CANDIDATE_GROUP, new ArrayList<>());
            variables.put(task.getId() + ASSIGNEE_LIST, new ArrayList<>());
            if (Objects.nonNull(extensionElements)) {
                List<CamundaProperties> camundaProperties = extensionElements.getElementsQuery()
                        .filterByType(CamundaProperties.class)
                        .list();
                List<CamundaProperty> properties = new ArrayList<>();
                camundaProperties.forEach(x -> properties.addAll(x.getCamundaProperties()));
                // 处理property参数
                for (CamundaProperty property : properties) {
                    String name = property.getCamundaName();
                    String value = property.getCamundaValue();
                    if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
                        continue;
                    }
                    log.info("=========================> 处理所有的特殊扩展变量 act_id: {} property name: {}, value: {}", task.getId(), name, value);
                    // 处理人
                    if (StringUtils.equals(name, ACTOR_SUBMIT_CANDIDATE)) {
                        getAssigneeVariableMap(procDefId, task, value, variables);
                    }
                }
            }
        }
        log.info("=========================> 处理所有的特殊扩展变量 结果: {}", JsonUtils.toJson(variables));
        return variables;
    }

    /**
     * 获取节点处理人的变量
     *
     * @param procDefId
     * @param task
     * @param value
     * @param variables
     */
    private void getAssigneeVariableMap(String procDefId, UserTask task, String value, VariableMap variables) {
        log.info("=========================> 处理所有的特殊扩展变量 procDefId: {} value: {}, variables: {}", procDefId, value, JsonUtils.toJson(variables));
        String actId = task.getId();
        List<ProcessAssigneeDTO> assigneeDTOS = JsonUtils.readValueList(value, ProcessAssigneeDTO.class);
        for (ProcessAssigneeDTO assigneeDTO : assigneeDTOS) {
            // 获取多实例数组信息
            List<String> multiPerson = (List<String>) variables.get(actId + ASSIGNEE_LIST);
            List<String> candidateGroups = (List<String>) variables.get(actId + CANDIDATE_GROUP);
            switch (assigneeDTO.getType()) {
                case $APPLICANT -> {
                    variables.put(actId + ASSIGNEE, LoginUserUtils.getLoginId());
                    log.info("=========================> 处理所有的特殊扩展变量 申请人=> 申请人节点：{}, 用户：{}", actId, LoginUserUtils.getLoginId());
                }
                case $USER -> {
                    if (CollectionUtils.isNotEmpty(assigneeDTO.getActorList())) {
                        List<ProcessAssigneeDTO.ActorDTO> actorList = JsonUtils.readValueList(JsonUtils.toJson(assigneeDTO.getActorList()), ProcessAssigneeDTO.ActorDTO.class);
                        List<String> assignees = actorList.stream().map(ProcessAssigneeDTO.ActorDTO::getId).collect(Collectors.toList());
                        List<UserRsp> users = userRestApi.listUsersByUserId(assignees).getList();
                        if (CollectionUtils.isEmpty(users)) {
                            throw new MagusException(ProcessResultEnum.RESULT_ERROR_USER_NOT_EXIST);
                        }
                        List<String> loginIds = users.stream().map(UserRsp::getLoginId).collect(Collectors.toList());
                        if (BooleanUtils.isTrue(processNodeService.isMultiInstance(procDefId, actId))) {
                            multiPerson.addAll(loginIds);
                        } else {
                            variables.put(actId + ASSIGNEE, loginIds.get(0));
                        }
                        log.info("=========================> 处理所有的特殊扩展变量 用户 => 节点：{}, 并发用户：{}, 指定用户：{}", actId, JsonUtils.toJson(multiPerson), assignees.get(0));
                    }
                }
                case $RELATION -> {
                    if (CollectionUtils.isNotEmpty(assigneeDTO.getActorList())) {
                        List<ProcessAssigneeDTO.ActorDTO> actors = JsonUtils.readValueList(JsonUtils.toJson(assigneeDTO.getActorList()), ProcessAssigneeDTO.ActorDTO.class);
                        handleRelation(procDefId, actId, actors, multiPerson, candidateGroups, variables);
                    }
                    log.info("=========================> 处理所有的特殊扩展变量 关系 => 节点：{}, 并发组织: {}", actId, JsonUtils.toJson(multiPerson));
                }
                case $ORG -> {
                    if (CollectionUtils.isNotEmpty(assigneeDTO.getActorList())) {
                        handleOrg(procDefId, actId, assigneeDTO, multiPerson, candidateGroups);
                    }
                    log.info("=========================> 处理所有的特殊扩展变量 组织 => 节点：{}, 并发组织: {}", actId, JsonUtils.toJson(multiPerson));
                }
                case $WORKGROUP -> {
                    if (CollectionUtils.isNotEmpty(assigneeDTO.getActorList())) {
                        List<ProcessAssigneeDTO.ActorDTO> actorList = JsonUtils.readValueList(JsonUtils.toJson(assigneeDTO.getActorList()), ProcessAssigneeDTO.ActorDTO.class);
                        List<String> workgroupIds = actorList.stream().map(ProcessAssigneeDTO.ActorDTO::getId).collect(Collectors.toList());
                        log.info("=========================> 处理所有的特殊扩展变量 工作组Id：{}", JsonUtils.toJson(workgroupIds));

                        if (BooleanUtils.isTrue(processNodeService.isMultiInstance(procDefId, actId))) {
                            List<WorkgroupUserRestRsp> workgroupUserRsps = workgroupRestApi.listByWorkGroupIds(workgroupIds).getList();
                            List<String> userIds = workgroupUserRsps.stream().map(WorkgroupUserRestRsp::getUserId).distinct().collect(Collectors.toList());
                            List<UserRsp> users = userRestApi.listUsersByUserId(userIds).getList();
                            multiPerson.addAll(users.stream().map(UserRsp::getLoginId).distinct().collect(Collectors.toList()));
                        } else {
                            workgroupIds.forEach(workgroupId -> candidateGroups.add(ProcessAssigneeTypeEnum.$WORKGROUP.getPre() + workgroupId));
                        }
                        log.info("=========================> 处理所有的特殊扩展变量 工作组 => 节点：{}, 并发组织: {}, 指定组织：{}", actId, JsonUtils.toJson(multiPerson), JsonUtils.toJson(workgroupIds));
                    }
                }
                case $DUTY -> {
                    if (CollectionUtils.isNotEmpty(assigneeDTO.getActorList())) {
                        List<ProcessAssigneeDTO.ActorDTO> actorList = JsonUtils.readValueList(JsonUtils.toJson(assigneeDTO.getActorList()), ProcessAssigneeDTO.ActorDTO.class);
                        List<String> dutyIds = actorList.stream().map(ProcessAssigneeDTO.ActorDTO::getId).collect(Collectors.toList());
                        log.info("=========================> 处理所有的特殊扩展变量 职务Id：{}", JsonUtils.toJson(dutyIds));

                        if (BooleanUtils.isTrue(processNodeService.isMultiInstance(procDefId, actId))) {
                            List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listOrgByDutyIds(dutyIds).getList();
                            List<String> userIds = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).distinct().collect(Collectors.toList());
                            List<UserRsp> users = userRestApi.listUsersByUserId(userIds).getList();
                            multiPerson.addAll(users.stream().map(UserRsp::getLoginId).distinct().collect(Collectors.toList()));
                        } else {
                            dutyIds.forEach(dutyId -> candidateGroups.add(ProcessAssigneeTypeEnum.$DUTY.getPre() + dutyId));
                        }
                        log.info("=========================> 处理所有的特殊扩展变量 职务 => 节点：{}, 并发组织: {}, 指定组织：{}", actId, JsonUtils.toJson(multiPerson), JsonUtils.toJson(dutyIds));
                    }
                }
                default -> {
                    log.warn("暂不支持");
                }
            }
            variables.put(actId + ASSIGNEE_LIST, multiPerson.stream().distinct().collect(Collectors.toList()));
            variables.put(actId + CANDIDATE_GROUP, candidateGroups.stream().distinct().collect(Collectors.toList()));
        }
    }

    /**
     * 处理组织
     *
     * @param procDefId
     * @param actId
     * @param assigneeDTO
     * @param multiPerson
     * @param candidateGroups
     */
    private void handleOrg(String procDefId, String actId, ProcessAssigneeDTO assigneeDTO, List<String> multiPerson, List<String> candidateGroups) {
        List<ProcessAssigneeDTO.ActorDTO> actors = JsonUtils.readValueList(JsonUtils.toJson(assigneeDTO.getActorList()), ProcessAssigneeDTO.ActorDTO.class);

        for (int i = 0; i < actors.size(); i++) {
            String orgId = actors.get(i).getId();
            List<ProcessAssigneeDTO.ActorDTO> duties = actors.get(i).getDuty();
            log.info("=========================> 处理组织 组织Id：{}, 职务：{}", orgId, JsonUtils.toJson(duties));
            List<String> dutyIds = duties.stream().filter(x -> StringUtils.isNotBlank(x.getId())).map(ProcessAssigneeDTO.ActorDTO::getId).collect(Collectors.toList());

            if (BooleanUtils.isTrue(processNodeService.isMultiInstance(procDefId, actId))) {
                List<OrgDutyUserRsp> orgDutyUserRsps = userRestApi.listByOrgIdAndDutyIdIn(orgId, dutyIds).getList();
                List<String> userIds = orgDutyUserRsps.stream().map(OrgDutyUserRsp::getUserId).distinct().collect(Collectors.toList());
                List<UserRsp> users = userRestApi.listUsersByUserId(userIds).getList();
                multiPerson.addAll(users.stream().map(UserRsp::getLoginId).distinct().collect(Collectors.toList()));
            } else {
                if (CollectionUtils.isNotEmpty(dutyIds)) {
                    for (String dutyId : dutyIds) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(ProcessAssigneeTypeEnum.$ORG.getPre()).append(orgId).append("@").append(dutyId);
                        candidateGroups.add(sb.toString());
                    }
                } else {
                    candidateGroups.add(ProcessAssigneeTypeEnum.$ORG.getPre() + orgId);
                }
            }
        }
        log.info("=========================> 处理组织 节点：{}, 指定组织：{}", actId, JsonUtils.toJson(candidateGroups));
    }

    /**
     * 处理关系
     *
     * @param procDefId
     * @param actId
     * @param actors
     * @param multiPerson
     * @param candidateGroups
     * @param variables
     */
    private void handleRelation(String procDefId, String actId, List<ProcessAssigneeDTO.ActorDTO> actors, List<String> multiPerson, List<String> candidateGroups, VariableMap variables) {
        for (int i = 0; i < actors.size(); i++) {
            String relationId = actors.get(i).getId();
            if (StringUtils.isBlank(relationId)) {
                continue;
            }
            List<ProcessAssigneeDTO.ActorDTO> duties = actors.get(i).getDuty();
            log.info("=========================> 处理关系 关系Id：{}, 职务：{}", relationId, JsonUtils.toJson(duties));
            List<String> dutyIds = null;
            if (CollectionUtils.isNotEmpty(duties)) {
                dutyIds = duties.stream().filter(x -> StringUtils.isNotBlank(x.getId())).map(ProcessAssigneeDTO.ActorDTO::getId).collect(Collectors.toList());
            }
            EntryRsp entry = dictRestApi.findEntryById(relationId).getData();
            String code = entry.getCode();
            ProcessAssigneeRelationEnum relation = ProcessAssigneeRelationEnum.findByCode(code);

            if (BooleanUtils.isTrue(processNodeService.isMultiInstance(procDefId, actId))) {
                // 多实例处理
                handleMultiRelation(multiPerson, dutyIds, relation);
                log.info("=========================> 处理关系 节点：{}, 并发用户：{}", actId, JsonUtils.toJson(multiPerson));
            } else {
                // 代理组处理
                handleGroupRelation(actId, candidateGroups, variables, dutyIds, relation);
            }
        }
    }

    /**
     * 关系代理组处理
     *
     * @param actId
     * @param candidateGroups
     * @param variables
     * @param dutyIds
     * @param relation
     */
    private void handleGroupRelation(String actId, List<String> candidateGroups, VariableMap variables, List<String> dutyIds, ProcessAssigneeRelationEnum relation) {
        if (ProcessAssigneeRelationEnum.APPLICANT == relation) {
            // 申请人
            variables.put(actId + ASSIGNEE, LoginUserUtils.getLoginId());
        } else if (ProcessAssigneeRelationEnum.applicantOrg(relation)) {
            // 申请人组织相关
            if (relation.getRelation() >= 0) {
                List<OrganizationRsp> orgs = organizationRestApi.listRelationOrg(LoginUserUtils.getId(), relation.getRelation()).getList();
                for (OrganizationRsp org : orgs) {
                    if (CollectionUtils.isNotEmpty(dutyIds)) {
                        for (String dutyId : dutyIds) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(ProcessAssigneeTypeEnum.$ORG.getPre()).append(org.getId()).append("@").append(dutyId);
                            candidateGroups.add(sb.toString());
                        }
                    } else {
                        candidateGroups.add(ProcessAssigneeTypeEnum.$ORG.getPre() + org.getId());
                    }
                }
            }
        }
        log.info("=========================> 处理关系代理组 节点：{}, 指定关系代理组：{}", actId, JsonUtils.toJson(candidateGroups));
    }

    /**
     * 多实例关系处理
     *
     * @param multiPerson
     * @param dutyIds
     * @param relation
     */
    private void handleMultiRelation(List<String> multiPerson, List<String> dutyIds, ProcessAssigneeRelationEnum relation) {
        if (ProcessAssigneeRelationEnum.APPLICANT == relation) {
            // 申请人即启动人
            multiPerson.add(LoginUserUtils.getLoginId());
        } else if (ProcessAssigneeRelationEnum.applicantOrg(relation)) {
            if (relation.getRelation() >= 0) {
                List<UserRsp> users = userRestApi.listRelationUsers(LoginUserUtils.getLoginId(), dutyIds, relation.getRelation()).getList();
                List<String> loginIds = users.stream().map(UserRsp::getLoginId).collect(Collectors.toList());
                multiPerson.addAll(loginIds);
            } else {
                multiPerson.add(LoginUserUtils.getLoginId());
            }
        } else {
            throw new MagusException("不支持");
        }
    }
}