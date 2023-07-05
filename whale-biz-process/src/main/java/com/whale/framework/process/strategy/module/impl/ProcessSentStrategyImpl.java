package com.whale.framework.process.strategy.module.impl;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.whale.framework.process.entity.ProcessDefinition;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.service.ProcessDefinitionService;
import com.whale.framework.process.service.ProcessInstanceService;
import com.whale.framework.process.strategy.module.IProcessStrategy;
import com.magus.framework.core.utils.ColumnUtils;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.generator.api.GeneratorPageRestApi;
import com.magus.framework.generator.api.dto.rsp.GeneratorPageRsp;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import com.magus.framework.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.impl
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
@Slf4j
public class ProcessSentStrategyImpl implements IProcessStrategy {

    @Autowired
    HistoryService historyService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    GeneratorPageRestApi generatorPageRestApi;

    @Override
    public Integer count() {
        log.info("=========================>已发 所有的任务计数 开始");
        List<SearchFilter> searchFilters = new ArrayList<>();

        searchFilters.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));
        searchFilters.add(SearchFilter.equal(ProcessInstance::getCreateId, LoginUserUtils.getLoginId()));
        searchFilters.add(SearchFilter.notEqual(ProcessInstance::getStatus, ProcessStatusEnum.UNCOMMITTED.getCode()));
        Specification<ProcessInstance> spec = JpaSearchUtils.buildAndSpec(searchFilters);
        // 获取所有历史任务实例
        long count = processInstanceService.count(spec);
        return (int) count;
    }

    @Override
    public PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req) {
        log.info("=========================> 已发 分页入参：{}", JsonUtils.toJson(req));
        Page<ProcessInstance> page = searchProcessInstance(req);
        List<ProcessInstance> sentInstances = page.getContent();
        log.info("=========================> 已发 分页结果：{}", JsonUtils.toJson(sentInstances));

        if (CollectionUtils.isEmpty(sentInstances)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }

        // 查询流程
        List<String> procDefIds = sentInstances.stream().map(ProcessInstance::getProcDefId).distinct().collect(Collectors.toList());
        List<ProcessDefinition> defs = processDefinitionService.findByProcDefIdIn(procDefIds);
        Map<String, ProcessDefinition> defMap = defs.stream().collect(toMap(ProcessDefinition::getProcDefId, Function.identity(), (x, y) -> x));

        // 数据转换
        List<ProcessInstanceRsp> res = MagusUtils.copyList(sentInstances, ProcessInstanceRsp.class);

        res.forEach(inst -> {
            ProcessDefinition processDefinition = Optional.ofNullable(defMap.get(inst.getProcDefId())).orElse(null);
            // 处理撤回权限
            if (BooleanUtils.isTrue(processDefinition.getWithdrawFlag())) {
                List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(inst.getProcInstId()).finished().list();
                if (CollectionUtils.isNotEmpty(hisTasks) && hisTasks.size() == 1) {
                    inst.setWithdrawFlag(Boolean.TRUE);
                }
            }
        });
        log.info("=========================> 已发 分页转换后结果：{}", JsonUtils.toJson(res));

        return new PageImpl<>(res, PageUtils.page(req), page.getTotalElements());
    }

    private Page<ProcessInstance> searchProcessInstance(ProcessSearchReq req) {
        req.setAttr(ColumnUtils.getFieldName(ProcessInstance::getStartTime));
        req.setSort(1);
        PageRequest pageRequest = PageUtils.page(req);
        Specification<ProcessInstance> spec = buildFilters(req);
        Page<ProcessInstance> page = this.processInstanceService.findAll(spec, pageRequest);
        return page;
    }


    private Specification<ProcessInstance> buildFilters(ProcessSearchReq req) {
        List<SearchFilter> searchFilters = new ArrayList<>();
        if (Objects.nonNull(req.getBeginTime())) {
            searchFilters.add(SearchFilter.ge(ProcessInstance::getCreateTime, new Date(req.getBeginTime())));
        }

        if (Objects.nonNull(req.getEndTime())) {
            searchFilters.add(SearchFilter.le(ProcessInstance::getCreateTime, new Date(req.getEndTime())));
        }
        searchFilters.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));
        searchFilters.add(SearchFilter.equal(ProcessInstance::getCreateId, LoginUserUtils.getLoginId()));
        searchFilters.add(SearchFilter.notEqual(ProcessInstance::getStatus, ProcessStatusEnum.UNCOMMITTED.getCode()));

        Specification<ProcessInstance> spec1 = JpaSearchUtils.buildAndSpec(searchFilters);
        Specification<ProcessInstance> spec2 = null;
        Specification<ProcessInstance> spec3 = null;

        if (StringUtils.isNotBlank(req.getKeyword())) {
            spec2 = JpaSearchUtils.buildAndSpec(Collections.singleton(SearchFilter.like(ProcessInstance::getTitle, req.getKeyword())));
            spec3 = JpaSearchUtils.buildAndSpec(Collections.singleton(SearchFilter.equal(ProcessInstance::getProcessNo, req.getKeyword())));
        }
        Specification<ProcessInstance> or = JpaSearchUtils.or(spec2, spec3);
        Specification<ProcessInstance> res = JpaSearchUtils.and(spec1, or);

        return res;
    }

    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + ProcessModuleEnum.SENT.getCode();
    }
}
