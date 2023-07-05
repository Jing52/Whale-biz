package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessTaskRestApi;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.service.ProcessTaskService;
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
@Api(tags = "流程中心")
@RestController
@RequestMapping("/process/task")
public class ProcessTaskController implements ProcessTaskRestApi {

    @Autowired
    ProcessTaskService processTaskService;

    @GetMapping("/running/find-by-procInstId/{procInstId}")
    @ApiOperation(value = "查询流程实例活动节点")
    @Override
    public ListRsp<ProcessNodeRsp> findRunningByProcInstId(@PathVariable String procInstId) {
        return new ListRsp<>(processTaskService.findActivityInstance(procInstId));
    }

    @PostMapping("/execute")
    @ApiOperation(value = "执行操作")
    @Override
    public CommonRsp execute(@RequestBody ProcessTaskActionReq req) {
        processTaskService.execute(req);
        return new CommonRsp();
    }

    @PostMapping("/batch/approve")
    @ApiOperation(value = "批量审批")
    public CommonRsp batchApprove(@RequestBody List<String> taskIds) {
        processTaskService.batchApprove(taskIds);
        return new CommonRsp();
    }
}
