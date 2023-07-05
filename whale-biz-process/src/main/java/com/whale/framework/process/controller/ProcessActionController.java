package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessActionRestApi;
import com.magus.framework.camunda.api.dto.req.ProcessActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeActionRsp;
import com.whale.framework.process.entity.ProcessAction;
import com.whale.framework.process.service.ProcessActionService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.ListRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.controller
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Api(tags = "流程操作")
@RestController
@RequestMapping("/process/action")
public class ProcessActionController {

    @Autowired
    ProcessActionService processActionService;

    @ApiOperation(value = "获取当前用户待办任务信息可执行的操作")
    @GetMapping("/list-by-taskId/{taskId}")
    @Override
    public ListRsp<ProcessNodeActionRsp> listByTaskId(@PathVariable("taskId") String taskId) {
        List<ProcessNodeActionRsp> actions = processActionService.listByTaskId(taskId);
        return new ListRsp<>(actions);
    }

    @ApiOperation(value = "获取当前用户待办任务信息可执行的操作")
    @PostMapping("/save-all")
    @Override
    public CommonRsp saveAll(@RequestBody List<ProcessActionReq> actions) {
        processActionService.saveAll(actions);
        return new CommonRsp();
    }

}
