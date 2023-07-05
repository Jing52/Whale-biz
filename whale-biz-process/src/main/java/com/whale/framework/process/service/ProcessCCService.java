package com.whale.framework.process.service;

import com.whale.framework.process.entity.ProcessCC;
import com.whale.framework.process.repository.ProcessCCRepository;
import com.magus.framework.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessCCService extends BaseService<ProcessCC, String> {

    @Autowired
    private ProcessCCRepository repository;

    public List<ProcessCC> findByProcDefIdAndNodeKey(String procDefId, String nodeKey) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(nodeKey)) {
            return null;
        }
        return repository.findByProcDefIdAndNodeKeyAndDeletedIsFalse(procDefId, nodeKey);
    }
}
