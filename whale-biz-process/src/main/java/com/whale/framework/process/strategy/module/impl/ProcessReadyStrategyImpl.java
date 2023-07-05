package com.whale.framework.process.strategy.module.impl;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.enums.ProcessModuleEnum;
import com.whale.framework.process.enums.ProcessStatusEnum;
import com.whale.framework.process.service.ProcessInstanceService;
import com.whale.framework.process.strategy.module.IProcessStrategy;
import com.magus.framework.core.utils.ColumnUtils;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.generator.api.GeneratorPageRestApi;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.impl
 * @Description: 待发
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
@Slf4j
public class ProcessReadyStrategyImpl implements IProcessStrategy {

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    GeneratorPageRestApi generatorPageRestApi;

    @Override
    public Integer count() {
        log.info("=========================> 待发 所有的任务计数 开始");
        List<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(SearchFilter.equal(ProcessInstance::getDeleted, 0));
        searchFilters.add(SearchFilter.equal(ProcessInstance::getCreateId, LoginUserUtils.getLoginId()));
        searchFilters.add(SearchFilter.in(ProcessInstance::getStatus, List.of(ProcessStatusEnum.UNCOMMITTED.getCode(), ProcessStatusEnum.REJECT.getCode())));

        Specification<ProcessInstance> spec = JpaSearchUtils.buildAndSpec(searchFilters);
        long count = processInstanceService.count(spec);
        // 获取所有历史任务实例
        return (int) count;
    }

    @Override
    public PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req) {
        log.info("=========================> 待发 分页入参：{}", JsonUtils.toJson(req));
        Page<ProcessInstance> page = searchProcessInstance(req);
        List<ProcessInstance> readyInstances = page.getContent();
        log.info("=========================> 待发 分页结果：{}", JsonUtils.toJson(readyInstances));

        if (CollectionUtils.isEmpty(readyInstances)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }

        // 数据转换
        List<ProcessInstanceRsp> res = MagusUtils.copyList(readyInstances, ProcessInstanceRsp.class);
        return new PageImpl<>(res, PageUtils.page(req), page.getTotalElements());
    }

    private Page<ProcessInstance> searchProcessInstance(ProcessSearchReq req) {
        req.setAttr(ColumnUtils.getFieldName(ProcessInstance::getCreateTime));
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
        searchFilters.add(SearchFilter.in(ProcessInstance::getStatus, List.of(ProcessStatusEnum.UNCOMMITTED.getCode(), ProcessStatusEnum.REJECT.getCode())));

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
        return CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + ProcessModuleEnum.READY.getCode();
    }
}
