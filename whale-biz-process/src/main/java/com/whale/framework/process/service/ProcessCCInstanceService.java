package com.whale.framework.process.service;

import com.whale.framework.process.entity.ProcessCCInstance;
import com.whale.framework.process.repository.ProcessCCInstanceRepository;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.service.BaseService;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessCCInstanceService extends BaseService<ProcessCCInstance, String> {

    @Autowired
    private ProcessCCInstanceRepository repository;

    public List<ProcessCCInstance> findByProcInstId(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return Lists.newArrayList();
        }
        return repository.findByProcInstIdAndStateAndDeletedIsFalse(procInstId, 1);
    }

    public List<ProcessCCInstance> findCarbonCopySelf(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return Lists.newArrayList();
        }
        return repository.findByProcInstIdAndUserIdAndStateAndDeletedIsFalseOrderByCreateTimeDesc(procInstId, LoginUserUtils.getLoginId(), 1);
    }

    public ProcessCCInstance findTopCarbonCopySelf(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return null;
        }
        return repository.findTop1ByProcInstIdAndUserIdAndStateAndDeletedIsFalseOrderByCreateTimeDesc(procInstId, LoginUserUtils.getLoginId(), 1);
    }
}
