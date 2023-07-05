package com.whale.framework.process.service;

import com.whale.framework.process.entity.ProcessSchedule;
import com.whale.framework.process.repository.ProcessScheduleRepository;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程节点定时实现
 * @Author: Whale
 * @Date: 2022/12/5 3:27 PM
 */
@Slf4j
@Service
@GlobalTransactional
public class ProcessScheduleService extends BaseService<ProcessSchedule, String> {

    @Autowired
    ProcessScheduleRepository repository;

    public ProcessSchedule findByProcDefIdAndNodeKey(String processDefinitionId, String nodeKey) {
        if (StringUtils.isBlank(processDefinitionId) || StringUtils.isBlank(nodeKey)) {
            return null;
        }
        return repository.findByProcDefIdAndNodeKeyAndDeletedIsFalse(processDefinitionId, nodeKey);
    }
}
