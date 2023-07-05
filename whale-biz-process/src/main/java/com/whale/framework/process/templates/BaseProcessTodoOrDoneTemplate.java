package com.whale.framework.process.templates;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessTaskRsp;
import com.whale.framework.process.dto.ProcessHistoricInstanceDTO;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.service.ProcessInstanceService;
import com.magus.framework.core.utils.ColumnUtils;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.impl.base
 * @Description: 我的待办/已办模板
 * @Author: Whale
 * @Date: 2022/12/5 2:21 PM
 */
@Service
@Slf4j
public abstract class BaseProcessTodoOrDoneTemplate {

    @Autowired
    ProcessInstanceService processInstanceService;

    /**
     * 获取所有历史任务实例
     *
     * @return
     */
    public abstract List<ProcessHistoricInstanceDTO> listHisTaskInst();

    /**
     * 获取所有历史任务实例
     *
     * @return
     */
    public abstract Specification<ProcessInstance> buildFilters(ProcessSearchReq req, List<String> proInstIds);

    /**
     * 模板
     */
    public final PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req) {
        log.info("=========================> 待办/已办 分页：{}", JsonUtils.toJson(req));
        // 获取所有历史任务实例
        List<ProcessHistoricInstanceDTO> tasks = listHisTaskInst();

        // 获取所有的流程实例
        List<String> proInstIds = tasks.stream().map(ProcessHistoricInstanceDTO::getProcInstId).distinct().collect(toList());

        if (CollectionUtils.isEmpty(proInstIds)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }

        List<ProcessInstance> processInstances = processInstanceService.findByProcInstIdIn(proInstIds);
        proInstIds = processInstances.stream().map(ProcessInstance::getProcInstId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(proInstIds)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }
        log.info("=========================> 待办/已办 所有的任务对应的流程实例id：{}", JsonUtils.toJson(proInstIds));

        // 分页查询待办实例
        Page<ProcessInstance> page = searchProcessInstance(req, proInstIds);
        List<ProcessInstance> content = Optional.ofNullable(page.getContent()).orElse(new ArrayList<>());

        if (CollectionUtils.isEmpty(content)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }
        log.info("=========================> 待办/已办 所有的任务对应的流程实例分页结果：{}", JsonUtils.toJson(content));

        Map<String, ProcessHistoricInstanceDTO> taskMap = tasks.stream().collect(
                toMap(ProcessHistoricInstanceDTO::getProcInstId, Function.identity(), (v1, v2) -> v2));

        // 转换成返回数据
        List<ProcessInstanceRsp> list = Lists.newArrayList();
        content.forEach(x -> {
            ProcessTaskRsp taskRsp = new ProcessTaskRsp();
            taskRsp.setTaskId(Optional.ofNullable(taskMap.get(x.getProcInstId())).orElse(new ProcessHistoricInstanceDTO()).getTaskId());
            ProcessInstanceRsp rsp = ProcessInstanceRsp.builder()
                    .id(x.getId())
                    .processNo(x.getProcessNo())
                    .title(x.getTitle())
                    .procInstId(x.getProcInstId())
                    .startUserId(x.getStartUserId())
                    .startUserName(x.getStartUserName())
                    .startTime(x.getStartTime())
                    .claimFlag(Optional.ofNullable(taskMap.get(x.getProcInstId())).orElse(new ProcessHistoricInstanceDTO()).getClaimFlag())
                    .delegateFlag(Optional.ofNullable(taskMap.get(x.getProcInstId())).orElse(new ProcessHistoricInstanceDTO()).getDelegateFlag())
                    .task(taskRsp)
                    .build();
            list.add(rsp);
        });

        return new PageImpl<>(list, PageUtils.page(req), page.getTotalElements());
    }

    private Page<ProcessInstance> searchProcessInstance(ProcessSearchReq req, List<String> proInstIds) {
        if (ProcessModuleEnum.TODO.getCode().equals(req.getSource())) {
            req.setAttr(ColumnUtils.getFieldName(ProcessInstance::getStartTime));
            req.setSort(1);
        } else if (ProcessModuleEnum.DONE.getCode().equals(req.getSource())) {
            req.setAttr(ColumnUtils.getFieldName(ProcessInstance::getUpdateTime));
            req.setSort(1);
        }
        PageRequest pageRequest = PageUtils.page(req);
        Specification<ProcessInstance> spec = buildFilters(req, proInstIds);
        Page<ProcessInstance> page = this.processInstanceService.findAll(spec, pageRequest);
        return page;
    }
}
