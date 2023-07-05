package com.whale.framework.process.strategy.module.impl;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.whale.framework.process.entity.ProcessCCInstance;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.service.ProcessCCInstanceService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
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
public class ProcessCCStrategyImpl implements IProcessStrategy {

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ProcessCCInstanceService processCCInstanceService;

    @Autowired
    GeneratorPageRestApi generatorPageRestApi;

    @Override
    public Integer count() {
        log.info("=========================> 传阅 所有的任务计数 开始");
        List<SearchFilter> filters = new ArrayList<>();
        filters.add(SearchFilter.equal(ProcessCCInstance::getUserId, LoginUserUtils.getLoginId()));
        filters.add(SearchFilter.equal(ProcessCCInstance::getDeleted, 0));
        filters.add(SearchFilter.equal(ProcessCCInstance::getState, 1));

        Specification<ProcessCCInstance> spec = JpaSearchUtils.buildAndSpec(filters);
        long count = processCCInstanceService.count(spec);
        return (int) count;
    }

    @Override
    public PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req) {
        log.info("=========================> 传阅 分页入参：{}", JsonUtils.toJson(req));
        // 获未失效的传阅
        List<SearchFilter> filters1 = new ArrayList<>();
        filters1.add(SearchFilter.equal(ProcessCCInstance::getUserId, LoginUserUtils.getLoginId()));
        filters1.add(SearchFilter.equal(ProcessCCInstance::getDeleted, 0));
        filters1.add(SearchFilter.equal(ProcessCCInstance::getState, 1));

        Specification<ProcessCCInstance> spec1 = JpaSearchUtils.buildAndSpec(filters1);
        PageRequest pageRequest = PageRequest.of(req.getCurPage() - 1, req.getPageSize(), Sort.by(ColumnUtils.getFieldName(ProcessCCInstance::getCreateTime)).descending());


        Page<ProcessCCInstance> page = processCCInstanceService.findAll(spec1, pageRequest);
        List<ProcessCCInstance> ccInstances = page.getContent();
        List<String> procInstIds = ccInstances.stream().map(ProcessCCInstance::getProcInstId).collect(Collectors.toList());

        // 分页
        List<SearchFilter> filters2 = new ArrayList<>();
        filters2.add(SearchFilter.in(ProcessInstance::getProcInstId, procInstIds));
        filters2.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));

        Page<ProcessInstance> processInstances = searchProcessInstance(req, procInstIds);

        List<ProcessInstance> instances = processInstances.getContent();

        log.info("=========================> 传阅 分页结果：{}", JsonUtils.toJson(instances));

        if (CollectionUtils.isEmpty(instances)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }

        List<ProcessInstanceRsp> res = MagusUtils.copyList(instances, ProcessInstanceRsp.class);
        return new PageImpl<>(res, PageUtils.page(req), page.getTotalElements());
    }

    private Page<ProcessInstance> searchProcessInstance(ProcessSearchReq req, List<String> procInstIds) {
        req.setAttr(ColumnUtils.getFieldName(ProcessInstance::getStartTime));
        req.setSort(1);
        PageRequest pageRequest = PageUtils.page(req);
        Specification<ProcessInstance> spec = buildFilters(req, procInstIds);
        Page<ProcessInstance> page = this.processInstanceService.findAll(spec, pageRequest);
        return page;
    }


    private Specification<ProcessInstance> buildFilters(ProcessSearchReq req, List<String> procInstIds) {
        List<SearchFilter> searchFilters = new ArrayList<>();

        if (Objects.nonNull(req.getBeginTime())) {
            searchFilters.add(SearchFilter.ge(ProcessInstance::getCreateTime, new Date(req.getBeginTime())));
        }

        if (Objects.nonNull(req.getEndTime())) {
            searchFilters.add(SearchFilter.le(ProcessInstance::getCreateTime, new Date(req.getEndTime())));
        }
        searchFilters.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));
        searchFilters.add(SearchFilter.in(ProcessInstance::getProcInstId, procInstIds));

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
        return CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + ProcessModuleEnum.CC.getCode();
    }
}
