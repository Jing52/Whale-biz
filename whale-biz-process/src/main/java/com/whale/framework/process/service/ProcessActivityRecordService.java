package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.rsp.ProcessActivityRecordRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessAppendixRsp;
import com.whale.framework.process.entity.ProcessActivityRecord;
import com.whale.framework.process.enums.ProcessActivityStatusEnum;
import com.whale.framework.process.repository.ProcessActivityRecordRepository;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import com.magus.framework.service.BaseService;
import com.magus.framework.system.api.AppendixRestApi;
import com.magus.framework.system.api.dto.rsp.AppendixRsp;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2022/12/16 9:23 AM
 */
@Service
@Slf4j
public class ProcessActivityRecordService extends BaseService<ProcessActivityRecord, String> {

    @Autowired
    private ProcessActivityRecordRepository repository;

    @Autowired
    private AppendixRestApi appendixRestApi;

    /**
     * 获取所有的活动动态
     *
     * @param procInstId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<ProcessActivityRecordRsp> listByProcInstId(String procInstId) {
        log.info("=========================> 根据流程id获取所有的活动动态 入参：{}", procInstId);
        if (StringUtils.isBlank(procInstId)) {
            return Lists.newArrayList();
        }

        List<ProcessActivityRecord> activities = repository.findByProcInstIdAndDeletedIsFalseOrderBySortAsc(procInstId);
        log.info("=========================> 根据流程id获取所有的活动动态 本地：{}", JsonUtils.toJson(activities));

        // 捞出附件信息
        List<String> appendixIdList = new ArrayList<>();
        activities.forEach(x -> {
            String appendixIds = x.getAppendixIds();
            if (StringUtils.isNotBlank(appendixIds)) {
                appendixIdList.addAll(Arrays.asList(appendixIds.split(",")));
            }
        });

        List<AppendixRsp> appendices = appendixRestApi.list(appendixIdList).getList();
        Map<String, AppendixRsp> appendixMap = appendices.stream().collect(Collectors.toMap(AppendixRsp::getId, Function.identity(), (o1, o2) -> o1));

        // 封装数据
        List<ProcessActivityRecordRsp> result = activities.stream().map(x -> {
            String appendixIds = x.getAppendixIds();
            List<ProcessAppendixRsp> appendixList = covert2Appendix(appendixMap, appendixIds);

            ProcessActivityRecordRsp activityRsp = new ProcessActivityRecordRsp();
            activityRsp.setCreateTime(x.getCreateTime());
            activityRsp.setProcInstId(x.getProcInstId());
            activityRsp.setStatus(ProcessActivityStatusEnum.valueOf(x.getActivityStatus()).getDesc());
            activityRsp.setOpinion(x.getOpinion());
            activityRsp.setAppendices(appendixList);
            activityRsp.setOperator(x.getOperator());
            return activityRsp;
        }).collect(Collectors.toList());
        log.info("=========================> 根据流程id获取所有的活动动态 结果：{}", JsonUtils.toJson(result));
        return result;
    }

    /**
     * 转换数据
     *
     * @param appendixMap
     * @param appendixIds
     * @return
     */
    private List<ProcessAppendixRsp> covert2Appendix(Map<String, AppendixRsp> appendixMap, String appendixIds) {
        if (MapUtils.isEmpty(appendixMap) || StringUtils.isBlank(appendixIds)) {
            return Lists.newArrayList();
        }
        List<String> appendixIdList = Arrays.asList(appendixIds.split(","));
        List<AppendixRsp> appendics = appendixIdList.stream().map(id -> appendixMap.get(id)).collect(Collectors.toList());
        List<ProcessAppendixRsp> res = MagusUtils.copyList(appendics, ProcessAppendixRsp.class);
        return res;
    }

    /**
     * 查询最近的处理中的动态
     *
     * @param procInstId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessActivityRecord findLastedPendingActivity(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return null;
        }

        List<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(SearchFilter.equal(ProcessActivityRecord::getDeleted, Boolean.FALSE));
        searchFilters.add(SearchFilter.equal(ProcessActivityRecord::getProcInstId, procInstId));
        searchFilters.add(SearchFilter.equal(ProcessActivityRecord::getActivityStatus, ProcessActivityStatusEnum.PENDING.getCode()));

        Specification<ProcessActivityRecord> spec = JpaSearchUtils.buildAndSpec(searchFilters);
        return repository.findOne(spec).orElse(null);
    }

    /**
     * 查询最近的动态
     *
     * @param procInstId
     * @return
     */
    public ProcessActivityRecord findLastedActivity(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return new ProcessActivityRecord();
        }
        ProcessActivityRecord activity = repository.findTop1ByProcInstIdOrderBySortDesc(procInstId);
        return activity;
    }
}
