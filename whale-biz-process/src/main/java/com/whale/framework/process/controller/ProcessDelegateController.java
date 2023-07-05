package com.whale.framework.process.controller;

import com.whale.framework.process.dto.req.ProcessDelegateReq;
import com.whale.framework.process.dto.req.ProcessDelegateSearchReq;
import com.whale.framework.process.dto.rsp.ProcessDelegateRsp;
import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.service.ProcessDelegateService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.DataRsp;
import com.magus.framework.core.dto.rsp.ListRsp;
import com.magus.framework.core.dto.rsp.PageRsp;
import com.magus.framework.system.api.dto.rsp.TreeNodeRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/process/delegate")
@Api(tags = "流程委托")
public class ProcessDelegateController extends BaseController<ProcessDelegate> {

    @Autowired
    private ProcessDelegateService processDelegateService;

    @PostMapping("page")
    @ApiOperation(value = "流程定义信息列表")
    public PageRsp<ProcessDelegateRsp> page(@RequestBody ProcessDelegateSearchReq req) {
        PageImpl<ProcessDelegateRsp> page = processDelegateService.page(req);
        return new PageRsp<>(req, page.getTotalElements(), page.getContent());
    }

    @GetMapping("/{batchNo}")
    @ApiOperation(value = "委托详情")
    public DataRsp<ProcessDelegateRsp> info(@PathVariable("batchNo") String batchNo) {
        ProcessDelegateRsp info = this.processDelegateService.info(batchNo);
        return new DataRsp(info);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增委托")
    public CommonRsp add(@RequestBody ProcessDelegateReq req) {
        this.processDelegateService.add(req);
        return new CommonRsp();
    }

    @PostMapping("/update-state")
    @ApiOperation(value = "委托/取消委托")
    public CommonRsp updateDelegateState(@RequestBody ProcessDelegateReq req) {
        this.processDelegateService.updateDelegateState(req);
        return new CommonRsp();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑")
    public CommonRsp update(@RequestBody ProcessDelegate req) {
        this.processDelegateService.update(req);
        return new CommonRsp();
    }

    @GetMapping("/group/tree")
    @ApiOperation(value = "委托功能组列表")
    public ListRsp<TreeNodeRsp> treeDelegateGroup() {
        return new ListRsp<>(processDelegateService.treeDelegateGroup());
    }

}
