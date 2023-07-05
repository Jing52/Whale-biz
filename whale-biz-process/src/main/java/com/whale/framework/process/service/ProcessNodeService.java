package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.dto.ProcessAssigneeDTO;
import com.whale.framework.process.entity.ProcessNode;
import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import com.whale.framework.process.repository.ProcessNodeDefinitionRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程节点定义实现
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Service
@Slf4j
public class ProcessNodeService extends BaseService<ProcessNode, String> {

    @Autowired
    ProcessNodeDefinitionRepository repository;

    @Autowired
    TaskService taskService;

    @Autowired
    RepositoryService repositoryService;

    public void saveAll(List<ProcessNode> actDefinitions) {
        this.repository.saveAll(actDefinitions);
    }

    /**
     * 查询未完成的节点定义(当前节点往后遇到条件则无法跳转)
     *
     * @param taskId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<ProcessNodeRsp> listNotRunningAct(String taskId) {
        log.info("=========================> 查询未完成的节点定义(当前节点往后遇到条件则无法跳转) 入参：{}", JsonUtils.toJson(taskId));
        if (StringUtils.isBlank(taskId)) {
            return new ArrayList<>();
        }
        List<String> jumpActIds = new ArrayList<>();

        Task task = taskService.createTaskQuery().taskId(taskId).active().singleResult();
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(task.getProcessDefinitionId());

        // 获取主流程
        Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        if (CollectionUtils.isEmpty(processes) || processes.size() != 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        Process process = processes.stream().findFirst().get();
        Collection<FlowElement> flowElements = process.getFlowElements();
        FlowElement startFlow = flowElements.stream().filter(flowElement -> ActivityTypes.START_EVENT.equals(flowElement.getElementType().getTypeName())).findFirst().get();

        Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
        List<StartEvent> start = startEvents.stream().filter(startEvent -> StringUtils.equals(startEvent.getId(), startFlow.getId())).collect(toList());

        Collection<SequenceFlow> outgoings = start.get(0).getOutgoing();

        // 开始节点的只会存在一个出口顺序流
        if (CollectionUtils.isEmpty(outgoings) || outgoings.size() > 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }

        // 递归获取节点
        getJumpNode(outgoings, task.getTaskDefinitionKey(), jumpActIds, Boolean.FALSE);

        List<ProcessNode> nodes = repository.findByProcDefIdAndNodeKeyInAndDeletedIsFalse(task.getProcessDefinitionId(), jumpActIds);
        return MagusUtils.copyList(nodes, ProcessNodeRsp.class);
    }

    private void getJumpNode(Collection<SequenceFlow> outgoings, String nodeKey, List<String> actIds, boolean add) {
        for (SequenceFlow sequenceFlow : outgoings) {
            ConditionExpression conditionExpression = sequenceFlow.getConditionExpression();
            if (Objects.nonNull(conditionExpression) && CollectionUtils.isNotEmpty(actIds)) {
                continue;
            }
            // 根据出口的顺序流获取目标节点
            FlowNode target = sequenceFlow.getTarget();
            if (add && target instanceof UserTask) {
                actIds.add(target.getId());
            }
            log.info("=========================> 递归进入下一个节点：{}", JsonUtils.toJson(target.getId()));
            if (StringUtils.equals(nodeKey, target.getId())) {
                getJumpNode(target.getOutgoing(), nodeKey, actIds, Boolean.TRUE);
            } else {
                getJumpNode(target.getOutgoing(), nodeKey, actIds, add);
            }
        }
    }

    /**
     * 判断是否是多实例节点
     *
     * @param procDefId
     * @param actId
     */
    public Boolean isMultiInstance(String procDefId, String actId) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(procDefId);

        Map<String, Boolean> map = new HashMap<>();
        map.put(actId, null);
        // 获取流程节点下的Process节点
        Collection<Process> processes = bpmnModelInstance.getModelElementsByType(Process.class);
        if (CollectionUtils.isNotEmpty(processes) && processes.size() == 1) {
            Process process = processes.stream().findFirst().get();
            Collection<FlowElement> flowElements = process.getFlowElements();
            handleProcess(flowElements, actId, map);
        }
        return map.get(actId);
    }

    /**
     * 判断节点是否在网关上
     *
     * @param procDefId
     * @param actId
     */
    public Boolean isGateway(String procDefId, String actId) {
        Boolean isGateway = Boolean.TRUE;
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(procDefId);
        ActivityImpl activity = processDefinition.findActivity(actId);

        List<PvmTransition> incomingTransitions = activity.getIncomingTransitions();

        for (PvmTransition incomingTransition : incomingTransitions) {
            FlowNode parentNode = (FlowNode) incomingTransition.getSource();
            // 判断父元素是否为网关类型
            if (parentNode instanceof ExclusiveGateway || parentNode instanceof InclusiveGateway) {
                // 节点在网关中
                isGateway = Boolean.TRUE;
                break;
            } else {
                // 节点不在网关中
                isGateway = Boolean.FALSE;
            }
        }
        return isGateway;
    }

    /**
     * 递归处理流元素
     *
     * @param flowElements
     * @param actId
     * @param map
     */
    private void handleProcess(Collection<FlowElement> flowElements, String actId, Map<String, Boolean> map) {
        Boolean multiFlag = map.get(actId);
        if (Objects.nonNull(multiFlag) || CollectionUtils.isEmpty(flowElements)) {
            return;
        }
        List<FlowElement> userTasks = flowElements.stream().filter(x -> (x instanceof UserTask) && StringUtils.equals(x.getId(), actId)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userTasks)) {
            // 存在当前节点的用户任务，校验是否是多实例节点
            UserTask userTask = (UserTask) userTasks.get(0);
            LoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
            if (Objects.nonNull(loopCharacteristics)) {
                map.put(actId, Boolean.TRUE);
                handleProcess(null, actId, map);
            } else {
                map.put(actId, Boolean.FALSE);
                handleProcess(null, actId, map);
            }
        } else {
            // 递归子流程
            List<FlowElement> subProcesses = flowElements.stream().filter(x -> (x instanceof SubProcess)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(subProcesses) && subProcesses.size() == 1) {
                SubProcess subProcess = (SubProcess) subProcesses.get(0);
                Collection<FlowElement> subFlowElements = subProcess.getFlowElements();
                handleProcess(subFlowElements, actId, map);
            }
        }
    }

    /**
     * 校验首个用户节点是申请人
     *
     * @param procDefId
     */
    public void checkApplicant(String procDefId) {
        log.info("=========================> 校验首个用户节点是申请人 入参： procDefId：{}", procDefId);
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(procDefId);

        // 获取流程节点下的Process节点
        Collection<Process> processes = bpmnModelInstance.getModelElementsByType(Process.class);
        log.info("=========================> 校验首个用户节点是申请人 流程：{}", processes.size());

        // 只会存在一个process
        if (CollectionUtils.isEmpty(processes) || processes.size() > 1) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
        }
        for (Process process : processes) {
            Collection<FlowElement> flowElements = process.getFlowElements();
            FlowElement startFlow = flowElements.stream().filter(flowElement -> ActivityTypes.START_EVENT.equals(flowElement.getElementType().getTypeName())).findFirst().get();

            // 未找到开始节点
            if (StringUtils.isBlank(startFlow.getId())) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
            }
            log.info("=========================> 校验首个用户节点是申请人 开始节点：{}", startFlow.getId());

            // 获取所有StartEvent节点,并且筛选出主流程的StartEvent节点
            Collection<StartEvent> startEvents = bpmnModelInstance.getModelElementsByType(StartEvent.class);

            String startEventId = startFlow.getId();
            List<StartEvent> masterProcessStartEvent = startEvents.stream().filter(x -> StringUtils.equals(startEventId, x.getId())).collect(Collectors.toList());
            log.info("=========================> 校验首个用户节点是申请人 主流程的开始节点：{}", masterProcessStartEvent.size());

            // 只会存在一个开始节点
            if (CollectionUtils.isEmpty(masterProcessStartEvent) || masterProcessStartEvent.size() > 1) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
            }

            for (StartEvent startEvent : masterProcessStartEvent) {
                // 获取StartEvent节点出口顺序流
                Collection<SequenceFlow> outgoings = startEvent.getOutgoing();

                // 开始节点的只会存在一个出口顺序流
                if (CollectionUtils.isEmpty(outgoings) || outgoings.size() > 1) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
                }
                for (SequenceFlow sequenceFlow : outgoings) {
                    // 根据出口的顺序流获取目标节点
                    FlowNode target = sequenceFlow.getTarget();

                    // 开始节点的下一个基点不是用户节点
                    if (!ActivityTypes.TASK_USER_TASK.equals(target.getElementType().getTypeName())) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
                    }

                    ExtensionElements extensionElements = target.getExtensionElements();

                    // 首个用户节点不存在扩展变量
                    if (Objects.isNull(extensionElements)) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
                    }

                    List<CamundaProperties> camundaProperties = extensionElements.getElementsQuery()
                            .filterByType(CamundaProperties.class)
                            .list();
                    log.info("=========================> 校验首个用户节点是申请人 申请人节点所有的扩展字段：{}", camundaProperties.size());
                    List<CamundaProperty> properties = new ArrayList<>();
                    camundaProperties.forEach(x -> properties.addAll(x.getCamundaProperties()));
                    // 处理property参数
                    List<CamundaProperty> actorProperties = properties.stream().filter(x -> StringUtils.equals(x.getCamundaName(), "actor_submit_candidate")).collect(Collectors.toList());

                    // 首个用户节点只会存在一个 actor_submit_candidate
                    if (CollectionUtils.isEmpty(actorProperties) || actorProperties.size() > 1) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
                    }
                    CamundaProperty camundaProperty = actorProperties.get(0);
                    String value = camundaProperty.getCamundaValue();
                    log.info("=========================> 校验首个用户节点是申请人 申请人节点扩展字段：{}", value);
                    List<ProcessAssigneeDTO> assigneeDTOS = JsonUtils.readValueList(value, ProcessAssigneeDTO.class);
                    List<ProcessAssigneeDTO> applicants = assigneeDTOS.stream().filter(x -> StringUtils.equals(ProcessAssigneeTypeEnum.$APPLICANT.getCode(), x.getType().getCode())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(applicants) || applicants.size() > 1) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_BPMN_DESIGN);
                    }
                }
            }
        }
    }

    /**
     * 查询流程定义下的用户节点
     *
     * @param processDefinitionId
     * @return
     */
    public List<ProcessNodeRsp> findByProcDefId(String processDefinitionId) {
        if (StringUtils.isBlank(processDefinitionId)) {
            return new ArrayList<>();
        }
        List<ProcessNode> nodes = repository.findByProcDefIdAndDeletedIsFalse(processDefinitionId);
        return MagusUtils.copyList(nodes, ProcessNodeRsp.class);
    }

    /**
     * 查询流程定义下的指定节点
     *
     * @param processDefinitionId
     * @return
     */
    public ProcessNodeRsp findByProcDefIdAndNodeKey(String processDefinitionId, String nodeKey) {
        if (StringUtils.isBlank(processDefinitionId) || StringUtils.isBlank(nodeKey)) {
            return null;
        }
        ProcessNode node = repository.findByProcDefIdAndNodeKeyAndDeletedIsFalse(processDefinitionId, nodeKey);
        return MagusUtils.copyProperties(node, ProcessNodeRsp.class);
    }

    /**
     * 获取申请人节点
     *
     * @param procDefId
     * @return
     */
    public ProcessNode getFirstUserTask(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            return null;
        }
        List<ProcessNode> nodes = repository.findByProcDefIdAndDeletedIsFalse(procDefId);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<ProcessNode> applicantNodes = nodes.stream().filter(node -> BooleanUtils.isTrue(node.getApplicant())).collect(toList());
            if (CollectionUtils.isNotEmpty(applicantNodes) && applicantNodes.size() == 1) {
                return applicantNodes.get(0);
            }
        }
        return null;
    }

    /**
     * 获取申请人节点
     *
     * @return
     */
    public List<ProcessNode> listApplicantNode(List<String> nodeKeys) {
        if (CollectionUtils.isEmpty(nodeKeys)) {
            return new ArrayList<>();
        }
        return repository.findByNodeKeyInAndApplicantIsTrueAndDeletedIsFalse(nodeKeys);
    }
}
