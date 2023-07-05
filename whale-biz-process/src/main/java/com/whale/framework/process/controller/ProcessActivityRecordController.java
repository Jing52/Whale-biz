package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessActivityRecordRestApi;
import com.magus.framework.camunda.api.dto.rsp.ProcessActivityRecordRsp;
import com.whale.framework.process.entity.ProcessActivityRecord;
import com.whale.framework.process.service.ProcessActivityRecordService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.ListRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.controller
 * @Description:
 * @Date: 2022/12/16 9:17 AM
 */
@Api(tags = "流程活动动态")
@Slf4j
@RestController
@RequestMapping("/process/activity/record")
public class ProcessActivityRecordController extends BaseController<ProcessActivityRecord> implements ProcessActivityRecordRestApi {

    @Autowired
    ProcessActivityRecordService processActivityRecordService;

    /* ====================================对外暴露API接口========================================= */

    @GetMapping("/list-by-procInstId/{procInstId}")
    @ApiOperation(value = "获取流程活动动态")
    @Override
    public ListRsp<ProcessActivityRecordRsp> listByProcInstId(@PathVariable("procInstId") String procInstId) {
        List<ProcessActivityRecordRsp> list = processActivityRecordService.listByProcInstId(procInstId);
        return new ListRsp<>(list);
    }
}
