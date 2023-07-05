package com.whale.framework.process.service;

import com.whale.framework.process.entity.ProcessInstanceAppendixRt;
import com.whale.framework.process.repository.ProcessInstanceAppendixRtRepository;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Service
public class ProcessInstanceAppendixRtService extends BaseService<ProcessInstanceAppendixRt, String> {

    @Autowired
    ProcessInstanceAppendixRtRepository repository;

    public List<ProcessInstanceAppendixRt> findByLocalProcInstId(String localProcInstId) {
        if (StringUtils.isBlank(localProcInstId)) {
            return Lists.newArrayList();
        }
        return repository.findByLocalProcInstIdAndDeletedIsFalse(localProcInstId);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    public void batchSaveAppendicesRt(String localProcInstId, List<String> appendixIds) {
        if (StringUtils.isBlank(localProcInstId) || CollectionUtils.isEmpty(appendixIds)) {
            return;
        }
        List<ProcessInstanceAppendixRt> rts = appendixIds.stream().map(appendixId -> {
            ProcessInstanceAppendixRt rt = new ProcessInstanceAppendixRt();
            rt.setLocalProcInstId(localProcInstId);
            rt.setAppendixId(appendixId);
            rt.setDeleted(Boolean.FALSE);
            return rt;
        }).collect(Collectors.toList());
        repository.saveAll(rts);
    }
}
