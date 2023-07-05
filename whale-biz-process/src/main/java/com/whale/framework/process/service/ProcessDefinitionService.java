package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.req.*;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.magus.framework.camunda.entity.*;
import com.whale.framework.process.entity.*;
import com.whale.framework.process.enums.ProcessScheduleTimeTypeEnum;
import com.whale.framework.process.repository.ProcessDefinitionRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.ColumnUtils;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.datasource.DynamicDataSourceUtil;
import com.magus.framework.datasource.JdbcTemplateActuator;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import com.magus.framework.service.BaseService;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import com.magus.framework.utils.PageUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程定义实现
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Slf4j
@Service
@GlobalTransactional
public class ProcessDefinitionService extends BaseService<ProcessDefinition, String> {

    @Autowired
    ProcessDefinitionRepository repository;

    @Autowired
    ProcessExtService processExtService;

    @Autowired
    ProcessNodeService processNodeService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    ProcessActionService processActionService;

    @Autowired
    ProcessNodeActionService processNodeActionService;

    @Autowired
    ProcessCCService processCcService;

    @Autowired
    ProcessFieldPermissionService processFieldPermissionService;

    @Autowired
    ProcessScheduleService processScheduleService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Autowired
    JdbcTemplateActuator jdbcTemplate;

    /**
     * key下的启用的流程定义
     *
     * @param procDefKeys
     * @return
     */
    public List<ProcessDefinition> findUpDefinitionByKeyIn(List<String> procDefKeys) {
        if (CollectionUtils.isEmpty(procDefKeys)) {
            return new ArrayList<>();
        }
        return repository.findByProcDefKeyInAndStateIsTrueAndDeletedIsFalse(procDefKeys);
    }

    /**
     * key下的启用的流程定义
     *
     * @param procDefKey
     * @return
     */
    public ProcessDefinition findUpDefinitionByKey(String procDefKey) {
        if (StringUtils.isBlank(procDefKey)) {
            return null;
        }
        return repository.findByProcDefKeyAndStateIsTrueAndDeletedIsFalse(procDefKey);
    }

    /**
     * 功能组下的流程定义
     *
     * @param sysGroupId
     * @return
     */
    public ProcessDefinitionRsp findUpDefinitionBySysGroupId(String sysGroupId) {
        if (StringUtils.isBlank(sysGroupId)) {
            return null;
        }
        AppGroupRsp group = groupRestApi.findOne(sysGroupId).getData();
        ProcessDefinition processDefinition = this.findUpDefinitionByKey(group.getProcDefKey());
        return MagusUtils.copyProperties(processDefinition, ProcessDefinitionRsp.class);
    }

    /**
     * 获取功能组下有效的流程
     *
     * @param sysGroupIds
     * @return
     */
    public List<ProcessDefinition> listUpDefinitionBySysGroupId(List<String> sysGroupIds) {
        if (CollectionUtils.isEmpty(sysGroupIds)) {
            return Lists.newArrayList();
        }
        List<AppGroupRsp> sysGroups = groupRestApi.listBySysGroupId(sysGroupIds).getList();
        List<String> procDefKeys = sysGroups.stream().map(AppGroupRsp::getProcDefKey).collect(toList());
        return repository.findByProcDefKeyInAndStateIsTrueAndDeletedIsFalse(procDefKeys);
    }

    /**
     * 批量查询流程定义
     *
     * @param procDefIds
     * @return
     */
    public List<ProcessDefinition> findByProcDefIdIn(List<String> procDefIds) {
        if (CollectionUtils.isEmpty(procDefIds)) {
            return new ArrayList<>();
        }
        return repository.findByProcDefIdInAndDeletedIsFalse(procDefIds);
    }

    /**
     * key下所有的流程定义
     *
     * @param procDefKey
     * @return
     */
    public List<ProcessDefinitionRsp> findByProcDefKey(String procDefKey) {
        if (StringUtils.isBlank(procDefKey)) {
            return new ArrayList<>();
        }
        List<ProcessDefinition> procDefs = repository.findByProcDefKeyAndDeletedIsFalse(procDefKey);
        return MagusUtils.copyList(procDefs, ProcessDefinitionRsp.class);
    }

    /**
     * 流程定义详情
     *
     * @param procDefId
     * @return
     */
    public ProcessDefinitionRsp findByProcDefId(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            return null;
        }
        ProcessDefinition processDefinition = repository.findByProcDefIdAndDeletedIsFalse(procDefId);
        return MagusUtils.copyProperties(processDefinition, ProcessDefinitionRsp.class);
    }

    /**
     * 预览
     *
     * @param processDefinitionId
     * @return
     */
    public ProcessDefinitionRsp preview(String processDefinitionId) {
        log.info("=========================> 流程预览 入参：{}", processDefinitionId);
        BpmnModelInstance modelInstance = this.repositoryService.getBpmnModelInstance(processDefinitionId);
        ProcessDefinitionRsp modelRsp = new ProcessDefinitionRsp();
        try {
            String modelStr = Bpmn.convertToString(modelInstance);
            log.info("=========================> 流程预览 xml对应的json字符串：{}", modelStr);
            modelRsp.setModels(modelStr);
        } catch (Exception e) {
            log.error("=========================> 预览流程图失败,processDefinitionId={}", processDefinitionId);
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROCESS_PARSE);
        }
        return modelRsp;
    }

    /**
     * 未被绑定的流程分页
     *
     * @return
     */
    public PageImpl<ProcessDefinitionRsp> pagePublishProcess(ProcessDefinitionSearchReq req) {
        log.info("=========================> 未被绑定的流程分页 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }
        List<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getDeleted, false));
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getState, true));
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getCreateId, LoginUserUtils.getLoginId()));
        // key
        if (StringUtils.isNotBlank(req.getProcDefKey())) {
            searchFilters.add(SearchFilter.equal(ProcessDefinition::getProcDefKey, req.getProcDefKey()));
        }
        // 名称
        if (StringUtils.isNotBlank(req.getProcDefName())) {
            searchFilters.add(SearchFilter.like(ProcessDefinition::getProcDefName, req.getProcDefName()));
        }

        Specification<ProcessDefinition> spec = JpaSearchUtils.buildAndSpec(searchFilters);

        // 排序
        req.setAttr(ColumnUtils.getFieldName(ProcessDefinition::getCreateTime));
        req.setSort(1);
        PageRequest pageRequest = PageUtils.page(req);

        Page<ProcessDefinition> page = this.repository.findAll(spec, pageRequest);
        log.info("=========================> 未被绑定的流程分页 分页结果：{}", JsonUtils.toJson(page.getContent()));

        List<ProcessDefinitionRsp> processDefinitionRsps = MagusUtils.copyList(page.getContent(), ProcessDefinitionRsp.class);

        // 查询所有的已被绑定的流程
        List<String> procDefKeys = processDefinitionRsps.stream().map(ProcessDefinitionRsp::getProcDefKey).collect(toList());
        List<AppGroupRsp> sysGroups = groupRestApi.listByProcDefKey(procDefKeys).getList();
        Map<String, AppGroupRsp> sysGroupMap = sysGroups.stream().collect(Collectors.toMap(AppGroupRsp::getProcDefKey, Function.identity(), (x, y) -> x));

        List<ProcessDefinitionRsp> result = page.getContent().stream().map(def -> {
            ProcessDefinitionRsp processDefinitionRsp = MagusUtils.copyProperties(def, ProcessDefinitionRsp.class);
            processDefinitionRsp.setRtFlag(Objects.nonNull(Optional.ofNullable(sysGroupMap.get(def.getProcDefKey())).orElse(null)));
            return processDefinitionRsp;
        }).collect(toList());
        log.info("=========================> 未被绑定的流程分页 最终结果：{}", JsonUtils.toJson(result));

        return new PageImpl<>(result, pageRequest, page.getTotalElements());
    }

    /**
     * key分页
     *
     * @param req
     * @return
     */
    public PageImpl<ProcessDefinitionRsp> pageKey(ProcessDefinitionSearchReq req) {
        log.info("=========================> 流程key分页 入参：{}", JsonUtils.toJson(req));
        // 1. 拼接查询sql
        String querySql = "SELECT proc_def_key FROM process_definition %s GROUP BY proc_def_key ORDER BY MIN( create_time ) DESC LIMIT %s, %s";
        String countSql = "SELECT count(1) FROM (SELECT distinct u.proc_def_key FROM ( SELECT * FROM process_definition %s ORDER BY create_time DESC ) u GROUP BY u.proc_def_key) tmp";
        String cond = "where deleted = 0 and create_id = '" + LoginUserUtils.getLoginId() + "'";
        if (StringUtils.isNotBlank(req.getProcDefKey())) {
            cond += " and proc_def_key = '" + req.getProcDefKey() + "'";
        }
        // 名称
        if (StringUtils.isNotBlank(req.getProcDefName())) {
            cond += " and proc_def_name like '%" + req.getProcDefName() + "%'";
        }
        querySql = String.format(querySql, cond, req.getStartNum(), req.getPageSize());
        countSql = String.format(countSql, cond);
        List<Map<String, Object>> resultList = jdbcTemplate.queryForListToDb(querySql, DynamicDataSourceUtil.getDbName());
        Integer total = jdbcTemplate.queryForObjectToDb(countSql, DynamicDataSourceUtil.getDbName(), Integer.class);
        log.info("=========================> 流程key分页 分页结果：{}， 总数:{}", JsonUtils.toJson(resultList), total);

        // 分页后的key的结果
        Column columnName = ColumnUtils.getColumnName(ProcessDefinition.class, ProcessDefinition::getProcDefKey, Column.class);
        List<String> procDefKeys = resultList.stream().map(map -> Optional.ofNullable(map.get(columnName.name())).map(Object::toString).orElse(null)).collect(Collectors.toList());

        List<ProcessDefinition> processDefinitions = repository.findByProcDefKeyInAndDeletedIsFalse(procDefKeys);
        log.info("=========================> 流程key分页 流程定义:{}", JsonUtils.toJson(processDefinitions));
        Map<String, ProcessDefinition> allProcessMap = processDefinitions.stream().collect(Collectors.toMap(ProcessDefinition::getProcDefKey, Function.identity(), (x, y) -> x));

        // 已启动的流程定义
        List<ProcessDefinition> runProcesses = this.findUpDefinitionByKeyIn(procDefKeys);
        log.info("=========================> 流程key分页 已启动的流程:{}", JsonUtils.toJson(runProcesses));
        Map<String, ProcessDefinition> runProcessMap = runProcesses.stream().collect(Collectors.toMap(ProcessDefinition::getProcDefKey, Function.identity(), (x, y) -> x));

        // 封装数据
        List<ProcessDefinitionRsp> result = procDefKeys.stream().map(procDefKey -> {
            ProcessDefinition runProcessDefinition = Optional.ofNullable(runProcessMap.get(procDefKey)).orElse(null);
            ProcessDefinitionRsp rsp = new ProcessDefinitionRsp();
            if (Objects.nonNull(runProcessDefinition)) {
                rsp = MagusUtils.copyProperties(runProcessDefinition, ProcessDefinitionRsp.class);
            } else {
                ProcessDefinition notRunProcessDefinition = Optional.ofNullable(allProcessMap.get(procDefKey)).orElse(null);
                rsp = ProcessDefinitionRsp.builder()
                        .procDefName(notRunProcessDefinition.getProcDefName())
                        .procDefKey(notRunProcessDefinition.getProcDefKey())
                        .build();
            }
            return rsp;
        }).collect(toList());
        log.info("=========================> 流程key分页 最终结果：{}", JsonUtils.toJson(result));

        return new PageImpl<>(result, PageUtils.page(req), total);
    }

    /**
     * id分页
     *
     * @param req
     * @return
     */
    public PageImpl<ProcessDefinitionRsp> pageId(ProcessDefinitionSearchReq req) {
        log.info("=========================> 流程id分页 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req) || StringUtils.isBlank(req.getProcDefKey())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        List<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getProcDefKey, req.getProcDefKey()));
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getDeleted, false));
        searchFilters.add(SearchFilter.equal(ProcessDefinition::getCreateId, LoginUserUtils.getLoginId()));
        // 名称
        if (StringUtils.isNotBlank(req.getProcDefName())) {
            searchFilters.add(SearchFilter.like(ProcessDefinition::getProcDefName, req.getProcDefName()));
        }
        // id
        if (StringUtils.isNotBlank(req.getProcDefId())) {
            searchFilters.add(SearchFilter.equal(ProcessDefinition::getProcDefId, req.getProcDefId()));
        }

        Specification<ProcessDefinition> spec = JpaSearchUtils.buildAndSpec(searchFilters);
        req.setAttr(ColumnUtils.getFieldName(ProcessDefinition::getVersion));
        req.setSort(1);
        PageRequest pageRequest = PageUtils.page(req);
        Page<ProcessDefinition> page = this.repository.findAll(spec, pageRequest);
        log.info("=========================> 流程id分页 最终结果：{}", JsonUtils.toJson(page.getContent()));

        return new PageImpl<>(MagusUtils.copyList(page.getContent(), ProcessDefinitionRsp.class), pageRequest, page.getTotalElements());
    }

    /**
     * 启用/关停
     *
     * @param processDefinitionId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public CommonRsp updateState(String processDefinitionId) {
        log.info("=========================> 启用/关停 入参：{}", processDefinitionId);
        ProcessDefinition processDefinition = repository.findByProcDefIdAndDeletedIsFalse(processDefinitionId);
        if (Objects.isNull(processDefinition)) {
            throw new MagusException(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST);
        }

        if (BooleanUtils.isTrue(processDefinition.getState())) {
            log.info("=========================> 启用/关停 关停");
            processDefinition.setState(Boolean.FALSE);
        } else if (BooleanUtils.isFalse(processDefinition.getState())) {
            // 校验操作流程对应的key是否存在已发布
            List<SearchFilter> searchFilters = new ArrayList<>();
            searchFilters.add(SearchFilter.equal(ProcessDefinition::getProcDefKey, processDefinition.getProcDefKey()));
            searchFilters.add(SearchFilter.equal(ProcessDefinition::getState, Boolean.TRUE));
            searchFilters.add(SearchFilter.equal(ProcessDefinition::getDeleted, false));
            Specification<ProcessDefinition> spec = JpaSearchUtils.buildAndSpec(searchFilters);
            long count = this.repository.count(spec);
            if (count > 0) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_CLOSE_UP_PROCESS);
            }

            log.info("=========================> 启用/关停 启用");
            processDefinition.setState(Boolean.TRUE);
        }
        this.repository.save(processDefinition);
        return new CommonRsp();
    }

    /**
     * 流程上传
     *
     * @param req
     * @param file
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessDefinitionRsp upload(ProcessDefinitionReq req, MultipartFile file) {
        log.info("=========================> 流程上传 入参：{}", JsonUtils.toJson(req));
        BpmnModelInstance modelInstance = null;
        org.camunda.bpm.engine.repository.ProcessDefinition definition = null;
        Deployment deploy = null;
        try {
            modelInstance = Bpmn.readModelFromStream(file.getInputStream());
            DeploymentBuilder addModelInstance = repositoryService.createDeployment()
                    .addModelInstance(file.getOriginalFilename(), modelInstance);
            deploy = addModelInstance.deployWithResult();
            log.info("=========================> 流程上传 流程部署完成 {}", JsonUtils.toJson(deploy.getId()));
        } catch (Exception e) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROCESS_PARSE, e.getMessage());
        }
        definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId()).singleResult();

        if (Objects.isNull(definition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        log.info("=========================> 流程上传 流程部署后流程定义：{}", JsonUtils.toJson(definition.getId()));

        // 保存流程定义
        ProcessDefinition entity = new ProcessDefinition();
        entity.setProcDefId(definition.getId());
        entity.setProcDefName(definition.getName());
        entity.setProcDefKey(definition.getKey());
        entity.setState(Boolean.FALSE);
        entity.setVersion(definition.getVersion());

        if (Objects.nonNull(req)) {
            // 扩展配置
            entity.setWithdrawFlag(req.getWithdrawFlag());
            entity.setCommentFlag(req.getCommentFlag());
            entity.setViewFlag(req.getViewFlag());
            entity.setCompleteRule(req.getCompleteRule());
            entity.setRejectRule(req.getRejectRule());
            entity.setPageFlag(req.getPageFlag());
            entity.setPageId(req.getPageId());
            entity.setPageUri(req.getPageUri());
        }
        ProcessDefinition save = this.repository.save(entity);


        // 解析流程图中节点用户任务信息
        // @formatter:off
        Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        if (CollectionUtils.isEmpty(processes) || processes.size() != 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        Process process = processes.stream().findFirst().get();
        Collection<FlowElement> flowElements = process.getFlowElements();
        FlowElement startFlow = flowElements.stream().filter(flowElement -> ActivityTypes.START_EVENT.equals(flowElement.getElementType().getTypeName())).findFirst().get();

        Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
        List<StartEvent> start = startEvents.stream().filter(startEvent -> StringUtils.equals(startEvent.getId(), startFlow.getId())).collect(toList());
        log.info("=========================> 流程上传 开始节点：{}", startFlow.getId());

        Collection<SequenceFlow> outgoings = start.get(0).getOutgoing();

        // 开始节点的只会存在一个出口顺序流
        if (CollectionUtils.isEmpty(outgoings) || outgoings.size() > 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        String startActId = null;
        for (SequenceFlow sequenceFlow : outgoings) {
            // 根据出口的顺序流获取目标节点
            FlowNode target = sequenceFlow.getTarget();
            // 开始节点的下一个基点不是用户节点
            if (!ActivityTypes.TASK_USER_TASK.equals(target.getElementType().getTypeName())) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
            }
            startActId = target.getId();
        }
        log.info("=========================> 流程上传 申请人节点：{}", startFlow.getId());

        // 节点信息转换
        String finalStartActId = startActId;
        String procDefId = definition.getId();
        String procDefName = definition.getName();
        List<ProcessNode> nodes = modelInstance.getModelElementsByType(UserTask.class)
                .stream().map(u -> {
                    ProcessNode node = new ProcessNode();
                    node.setNodeKey(u.getId());
                    node.setNodeName(u.getName());
                    node.setNodeType(Objects.nonNull(u.getLoopCharacteristics()) ? ActivityTypes.MULTI_INSTANCE_BODY : ActivityTypes.TASK_USER_TASK);
                    node.setAssignee(u.getCamundaAssignee());
                    node.setCandidateUsers(u.getCamundaCandidateUsers());
                    node.setCandidateGroups(u.getCamundaCandidateGroups());
                    node.setProcDefId(procDefId);
                    node.setProcDefName(procDefName);
                    node.setPageFlag(true);
                    node.setPageId(null);
                    node.setApplicant(StringUtils.equals(finalStartActId, u.getId()));
                    return node;
                }).collect(toList());
        log.info("=========================> 流程上传 所有的节点：{}", JsonUtils.toJson(nodes));
        this.processNodeService.saveAll(nodes);
        return MagusUtils.copyProperties(save, ProcessDefinitionRsp.class);
    }

    /**
     * 流程上传
     *
     * @param file
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessDefinitionRsp uploadTmp(ProcessDefinitionReq req, MultipartFile file) {
        log.info("=========================> 流程上传 入参：{}", JsonUtils.toJson(req));
        BpmnModelInstance modelInstance = null;
        org.camunda.bpm.engine.repository.ProcessDefinition definition = null;
        Deployment deploy = null;
        try {
            modelInstance = Bpmn.readModelFromStream(file.getInputStream());
            DeploymentBuilder addModelInstance = repositoryService.createDeployment()
                    .addModelInstance(file.getOriginalFilename(), modelInstance);
            deploy = addModelInstance.deployWithResult();
            log.info("=========================> 流程上传 流程部署完成 {}", JsonUtils.toJson(deploy));
        } catch (Exception e) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROCESS_PARSE, e.getMessage());
        }
        definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId()).singleResult();

        if (Objects.isNull(definition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        log.info("=========================> 流程上传 流程部署后流程定义：{}", JsonUtils.toJson(definition));

        // 保存流程定义
        ProcessDefinition entity = new ProcessDefinition();
        entity.setProcDefId(definition.getId());
        entity.setProcDefName(definition.getName());
        entity.setProcDefKey(definition.getKey());
        entity.setState(Boolean.FALSE);
        entity.setVersion(definition.getVersion());

        if (Objects.nonNull(req)) {
            // 扩展配置
            entity.setWithdrawFlag(req.getWithdrawFlag());
            entity.setCommentFlag(req.getCommentFlag());
            entity.setViewFlag(req.getViewFlag());
            entity.setCompleteRule(req.getCompleteRule());
            entity.setRejectRule(req.getRejectRule());
            entity.setPageFlag(req.getPageFlag());
            entity.setPageId(req.getPageId());
            entity.setPageUri(req.getPageUri());
        }
        ProcessDefinition save = this.repository.save(entity);


        // 解析流程图中节点用户任务信息
        // @formatter:off
        Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        if (CollectionUtils.isEmpty(processes) || processes.size() != 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        Process process = processes.stream().findFirst().get();
        Collection<FlowElement> flowElements = process.getFlowElements();
        FlowElement startFlow = flowElements.stream().filter(flowElement -> ActivityTypes.START_EVENT.equals(flowElement.getElementType().getTypeName())).findFirst().get();

        Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
        List<StartEvent> start = startEvents.stream().filter(startEvent -> StringUtils.equals(startEvent.getId(), startFlow.getId())).collect(toList());
        log.info("=========================> 流程上传 开始节点：{}", startFlow.getId());

        Collection<SequenceFlow> outgoings = start.get(0).getOutgoing();

        // 开始节点的只会存在一个出口顺序流
        if (CollectionUtils.isEmpty(outgoings) || outgoings.size() > 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        String startActId = null;
        for (SequenceFlow sequenceFlow : outgoings) {
            // 根据出口的顺序流获取目标节点
            FlowNode target = sequenceFlow.getTarget();
            // 开始节点的下一个基点不是用户节点
            if (!ActivityTypes.TASK_USER_TASK.equals(target.getElementType().getTypeName())) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
            }
            startActId = target.getId();
        }
        log.info("=========================> 流程上传 申请人节点：{}", startFlow.getId());

        // 节点信息转换
        String finalStartActId = startActId;
        String procDefId = definition.getId();
        String procDefName = definition.getName();
        List<ProcessNode> nodes = modelInstance.getModelElementsByType(UserTask.class)
                .stream().map(u -> {
                    ProcessNode node = new ProcessNode();
                    node.setNodeKey(u.getId());
                    node.setNodeName(u.getName());
                    node.setNodeType(Objects.nonNull(u.getLoopCharacteristics()) ? ActivityTypes.MULTI_INSTANCE_BODY : ActivityTypes.TASK_USER_TASK);
                    node.setAssignee(u.getCamundaAssignee());
                    node.setCandidateUsers(u.getCamundaCandidateUsers());
                    node.setCandidateGroups(u.getCamundaCandidateGroups());
                    node.setProcDefId(procDefId);
                    node.setProcDefName(procDefName);
                    node.setApplicant(StringUtils.equals(finalStartActId, u.getId()));
                    return node;
                }).collect(toList());

        // 保存申请人节点字段权限
        saveFieldPermission(procDefId, startActId, req.getFieldsPermission());

        List<ProcessNodeDefinitionReq> nodeReqs = req.getNodes();
        if (CollectionUtils.isNotEmpty(nodeReqs)) {
            // 处理节点信息
            Map<String, ProcessNodeDefinitionReq> nodeMap = nodeReqs.stream().collect(Collectors.toMap(ProcessNodeDefinitionReq::getNodeKey, Function.identity(), (x, y) -> x));
            nodes.forEach(node -> {
                ProcessNodeDefinitionReq nodeReq = Optional.ofNullable(nodeMap.get(node.getNodeKey())).orElse(new ProcessNodeDefinitionReq());
                node.setOpinionFlag(nodeReq.getOpinionFlag());
                node.setSignatureFlag(nodeReq.getSignatureFlag());
                node.setPageFlag(nodeReq.getPageFlag());
                node.setPageId(nodeReq.getPageId());
                node.setPageUri(nodeReq.getPageUri());
                node.setCcFlag(nodeReq.getCcFlag());
            });
            log.info("=========================> 流程上传 所有的节点：{}", JsonUtils.toJson(nodes));
            this.processNodeService.saveAll(nodes);
            // 处理节点
            for (ProcessNodeDefinitionReq nodeReq : nodeReqs) {
                // 处理节点扩展的功能
                handleNodeExt(procDefId, nodeReq);
            }
        }
        return MagusUtils.copyProperties(save, ProcessDefinitionRsp.class);
    }

    /**
     * 处理节点扩展的功能
     *
     * @param procDefId
     * @param nodeReq
     */
    private void handleNodeExt(String procDefId, ProcessNodeDefinitionReq nodeReq) {
        String nodeKey = nodeReq.getNodeKey();

        List<ProcessActionReq> actions = nodeReq.getActions();
        if (CollectionUtils.isNotEmpty(actions)) {
            // 保存节点的操作
            saveAction(procDefId, nodeKey, actions);
        }

        if (BooleanUtils.isTrue(nodeReq.getCcFlag())) {
            // 保存抄送
            saveCc(procDefId, nodeKey, nodeReq);
        }

        if (CollectionUtils.isNotEmpty(nodeReq.getFieldsPermission())) {
            // 保存节点字段权限
            saveFieldPermission(procDefId, nodeReq.getNodeKey(), nodeReq.getFieldsPermission());
        }

        if (Objects.nonNull(nodeReq.getSchedule())) {
            // 定时
            saveSchedule(procDefId, nodeReq.getNodeKey(), nodeReq.getSchedule());
        }
    }

    /**
     * 保存节点的操作
     *
     * @param procDefId
     * @param nodeKey
     * @param actions
     */
    private void saveAction(String procDefId, String nodeKey, List<ProcessActionReq> actions) {
        log.info("=========================> 保存节点的操作 入参： procDefId：{}， nodeKey： {}, actions: {}", procDefId, nodeKey, JsonUtils.toJson(actions));
        // 保存新增的操作
        List<ProcessAction> processActions = MagusUtils.copyList(actions, ProcessAction.class);
        List<ProcessAction> saveActions = processActions.stream().filter(action -> BooleanUtils.isFalse(action.getSysFlag())).distinct().collect(toList());

        List<ProcessAction> existActions = processActionService.findExistActions(saveActions);
        if (CollectionUtils.isNotEmpty(existActions)) {
            saveActions.removeAll(existActions);
        }
        log.info("=========================> 保存节点的操作 待保存的操作: {}", JsonUtils.toJson(saveActions));
        processActionService.saveAll(saveActions);
        // 处理流程节点操作权限
        List<ProcessNodeAction> nodeActions = actions.stream().map(action -> {
            ProcessNodeAction nodeAction = new ProcessNodeAction();
            nodeAction.setProcDefId(procDefId);
            nodeAction.setNodeKey(nodeKey);
            nodeAction.setAliasName(action.getAliasName());
            nodeAction.setActionCode(action.getCode());
            nodeAction.setEnable(action.getEnable());
            return nodeAction;
        }).collect(toList());
        log.info("=========================> 保存节点的操作 待保存的操作权限: {}", JsonUtils.toJson(nodeActions));
        processNodeActionService.saveAll(nodeActions);
    }

    /**
     * 保存抄送
     *
     * @param procDefId
     * @param nodeKey
     * @param nodeReq
     */
    private void saveCc(String procDefId, String nodeKey, ProcessNodeDefinitionReq nodeReq) {
        log.info("=========================> 保存抄送 入参： procDefId：{}， nodeKey： {}, nodeReq: {}", procDefId, nodeKey, JsonUtils.toJson(nodeReq));
        List<String> ccIds = nodeReq.getCcIds();
        List<ProcessCC> processCcs = ccIds.stream().map(ccId -> {
            ProcessCC processCc = new ProcessCC();
            processCc.setNodeKey(nodeKey);
            processCc.setProcDefId(procDefId);
            processCc.setUserId(ccId);
            return processCc;
        }).collect(toList());
        log.info("=========================> 保存节点的操作 待保存的抄送: {}", JsonUtils.toJson(processCcs));
        processCcService.saveAll(processCcs);
    }

    /**
     * 保存字段权限
     *
     * @param procDefId
     * @param nodeKey
     * @param fieldsPermission
     */
    private void saveFieldPermission(String procDefId, String nodeKey, List<ProcessFieldPermissionReq> fieldsPermission) {
        log.info("=========================> 保存字段权限 入参： procDefId：{}， nodeKey： {}, fieldsPermission: {}", procDefId, nodeKey, JsonUtils.toJson(fieldsPermission));
        List<ProcessFieldPermission> procPermissions = new ArrayList<>();
        for (ProcessFieldPermissionReq procFieldPermission : fieldsPermission) {
            ProcessFieldPermission permission = new ProcessFieldPermission();
            permission.setProcDefId(procDefId);
            permission.setNodeKey(nodeKey);
            permission.setPageId(procFieldPermission.getPageId());
            permission.setFieldName(procFieldPermission.getFieldName());
            permission.setPermission(procFieldPermission.getPermission());
            procPermissions.add(permission);
        }
        log.info("=========================> 保存字段权限 待保存: {}", JsonUtils.toJson(procPermissions));
        processFieldPermissionService.saveAll(procPermissions);
    }

    /**
     * 保存定时的配置
     *
     * @param schedule
     */
    private void saveSchedule(String procDefId, String nodeKey, ProcessScheduleReq schedule) {
        log.info("=========================> 保存字段权限 入参： procDefId：{}， nodeKey： {}, schedule: {}", procDefId, nodeKey, JsonUtils.toJson(schedule));
        ProcessSchedule processSchedule = new ProcessSchedule();
        processSchedule.setProcDefId(procDefId);
        processSchedule.setNodeKey(nodeKey);
        // 处理节点处理截止时间
        if (StringUtils.isBlank(schedule.getTimeType())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        processSchedule.setTimeType(schedule.getTimeType());
        switch (ProcessScheduleTimeTypeEnum.valueOf(schedule.getTimeType())) {
            case CUSTOM -> {
                if (schedule.getCustom() == null) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }
                processSchedule.setAfterTime(schedule.getCustom().getOffsetTime().getOffset());
                processSchedule.setAfterUnit(schedule.getCustom().getOffsetTime().getTimeUnit().name());
            }
            case FORM -> {
                if (schedule.getForm() == null) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }
                processSchedule.setColumnName(schedule.getForm().getColumnName());
            }
            default -> {
                throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
            }
        }
        // 超时后处理规则
        if (StringUtils.isBlank(schedule.getTimeoutType())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        processSchedule.setTimeoutType(schedule.getTimeoutType());

        if (schedule.getRemind() == null) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        processSchedule.setHandleType(schedule.getRemind().getHandleType());
        processSchedule.setAutoType(schedule.getRemind().getAutoType());
        processSchedule.setOffsetTime(schedule.getRemind().getOffsetTime().getOffset());
        processSchedule.setOffsetUnit(schedule.getRemind().getOffsetTime().getTimeUnit().name());
        processSchedule.setRemindMessage(schedule.getRemind().getRemindMessage());
        processSchedule.setReverseType(schedule.getRemind().getReverseType());
        log.info("=========================> 保存字段权限 待保存定时器: {}", JsonUtils.toJson(processSchedule));
        processScheduleService.save(processSchedule);
    }

    /**
     * 流程仿真
     *
     * @param procDefId
     * @param ext
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void test(String procDefId, Map<String, Object> ext) {
        log.info("=========================> 流程模拟仿真 入参： procDefId：{}， ext: {}", procDefId, JsonUtils.toJson(ext));
        try {
            ProcessDefinitionRsp definition = findByProcDefId(procDefId);
            // 1. 校验第一个节点是否设置的是申请人
            processNodeService.checkApplicant(procDefId);

            // 2. 获取所有处理人的表达式及对应数据
            VariableMap variables = Variables.createVariables();
            variables.putAll(ext);
            VariableMap variableMap = processExtService.handleAllUserTaskExtVariables(procDefId);
            variables.putAll(variableMap);
            log.info("=========================> 流程模拟仿真 处理人参数: {}", procDefId, JsonUtils.toJson(variables));

            // 3. 获取所有顺序流上的表达式并赋予模拟值
            BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(procDefId);

            // 获取所有顺序流
            Collection<SequenceFlow> sequenceFlows = bpmnModelInstance.getModelElementsByType(SequenceFlow.class);

            // 遍历所有顺序流，获取表达式
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                ConditionExpression conditionExpression = sequenceFlow.getConditionExpression();
                if (Objects.nonNull(conditionExpression)) {
                    String conditionExpressionText = conditionExpression.getTextContent();
                    String patternStr = "\\$\\{([a-zA-Z0-9]+)\\}";
                    Pattern pattern = Pattern.compile(patternStr);
                    Matcher matcher = pattern.matcher(conditionExpressionText);

                    while (matcher.find()) {
                        String variableName = matcher.group(1);
                        log.info("=========================> 流程模拟仿真 sequence flow id: " + sequenceFlow.getId() + "; condition expression: " + variableName);
                        variables.put(variableName, null);
                    }

                }
            }

            // 4. 模拟启动一个流程
            log.info("=========================> 流程模拟仿真 开始启动");
            ProcessInstance processInstance = processInstanceService.startInstance(processInstanceService.mockSaveDraft(definition.getProcDefId()), variables);

            log.info("=========================> 流程模拟仿真 删除Camunda流程");
            runtimeService.deleteProcessInstance(processInstance.getProcInstId(), "测试流程");

            log.info("=========================> 流程模拟仿真 删除本地流程");
            processInstance.setDeleted(Boolean.TRUE);
            processInstanceService.save(processInstance);

            log.info("=========================> 流程模拟仿真 删除本地任务");
            List<ProcessTask> tasks = processTaskService.findByProcInstId(processInstance.getProcInstId());
            tasks.forEach(task -> task.setDeleted(Boolean.TRUE));
            processTaskService.saveAll(tasks);

//            // 5. 删除测试流程，防止运行中的参数表过多
//            List<Task> tasks = new ArrayList<>();
//            do {
//                tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcInstId()).list();
//                if (CollectionUtils.isNotEmpty(tasks)) {
//                    for (Task task : tasks) {
//                        taskService.complete(task.getId());
//                    }
//                }
//            } while (CollectionUtils.isEmpty(tasks));
        } catch (Exception e) {
            throw new MagusException(e.getMessage());
        }
    }

    /**
     * 流程定义删除
     *
     * @param procDefIds
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteByProcDefId(List<String> procDefIds) {
        log.info("=========================> 流程定义删除 入参：{}", JsonUtils.toJson(procDefIds));
        if (CollectionUtils.isEmpty(procDefIds)) {
            return;
        }
        // 存在流程实例
        List<ProcessInstanceRsp> instances = processInstanceService.findByProcDefIdIn(procDefIds);
        if (CollectionUtils.isNotEmpty(instances)) {
            log.info("=========================> 流程定义删除 定义下运行过的流程实例：{}", JsonUtils.toJson(instances));
            // 存在运行中的流程实例，无法删除
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_INST_EXIST);
        }
        // 已启用
        List<ProcessDefinition> upProcessDefinitions = repository.findByProcDefIdInAndStateIsTrueAndDeletedIsFalse(procDefIds);
        if (CollectionUtils.isNotEmpty(upProcessDefinitions)) {
            log.info("=========================> 流程定义删除 已启用的流程定义：{}", JsonUtils.toJson(upProcessDefinitions));
            // 已启用，无法删除
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_UP);
        }
        // 查询并删除
        List<ProcessDefinition> processDefinitions = repository.findByProcDefIdInAndDeletedIsFalse(procDefIds);
        processDefinitions.forEach(processDefinition -> processDefinition.setDeleted(Boolean.TRUE));
        saveAll(processDefinitions);
        log.info("=========================> 流程定义删除结束");
    }
}
