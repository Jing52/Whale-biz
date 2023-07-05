package com.whale.framework.process.processor;

import com.whale.framework.process.entity.ProcessAction;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.service.ProcessActionService;
import com.magus.framework.datasource.SlaveDatabaseActuator;
import com.magus.framework.processor.AppResourceAutoCreate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description GeneratorAppResourceAutoCreate
 * @Author zhouyw
 * @Date 2022/12/9 11:57
 **/
@Slf4j
@Component
public class CamundaAppResourceAutoCreate extends AppResourceAutoCreate {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        super.applicationContext = event.getApplicationContext();

        super.onApplicationEvent(event);

        SlaveDatabaseActuator slaveActuator = applicationContext.getBean(SlaveDatabaseActuator.class);
        // 生成camunda单独库的表结构
        slaveActuator.createProcessDB();

        // 初始化操作表数据
        initAction();
    }

    private void initAction() {
        ProcessActionService actionService = applicationContext.getBean(ProcessActionService.class);
        List<ProcessAction> actionList = actionService.findSystemAction(ProcessActionEnum.getSystemAction());

        List<ProcessAction> actions = ProcessActionEnum.getSystemAction().stream().map(action -> {
            ProcessAction processAction = new ProcessAction();
            processAction.setCode(action.name());
            processAction.setName(action.getDesc());
            processAction.setSysFlag(Boolean.TRUE);
            return processAction;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(actionList)) {
            actionService.saveAll(actions);
        } else {
            List<ProcessAction> saveActions = actions.stream().filter(processAction -> !actionList.stream().anyMatch(x -> StringUtils.equals(x.getCode(), processAction.getCode()))).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(saveActions)) {
                actionService.saveAll(saveActions);
            }
        }
    }

}
