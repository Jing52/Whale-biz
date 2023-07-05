package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessDefinitionRestApi;
import com.magus.framework.camunda.api.dto.req.ProcessDefinitionReq;
import com.magus.framework.camunda.api.dto.req.ProcessDefinitionSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.entity.ProcessDefinition;
import com.whale.framework.process.service.ProcessDefinitionService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.DataRsp;
import com.magus.framework.core.dto.rsp.ListRsp;
import com.magus.framework.core.dto.rsp.PageRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Api(tags = "流程定义")
@Slf4j
@RestController
@RequestMapping("/process/definition")
public class ProcessDefinitionController extends BaseController<ProcessDefinition> implements ProcessDefinitionRestApi {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @ApiOperation(value = "流程定义信息")
    @GetMapping("/find-by-sysGroupId/{sysGroupId}")
    public DataRsp<ProcessDefinitionRsp> findBySysGroupId(@PathVariable("sysGroupId") String sysGroupId) {
        ProcessDefinitionRsp processDefinition = processDefinitionService.findUpDefinitionBySysGroupId(sysGroupId);
        return new DataRsp(processDefinition);
    }

//    @PostMapping("/find-effective-def/{userId}")
//    @ApiOperation(value = "查找有效的流程定义")
//    public ListRsp<ProcessDefinitionRsp> findEffectiveDelegateDefinition(@PathVariable("userId") String userId) {
//        List<ProcessDefinitionRsp> defs = this.processDefinitionService.findEffectiveDelegateDefinition(userId);
//        return new ListRsp<>(defs);
//    }


    @ApiOperation(value = "预览流程图")
    @GetMapping("/preview/{processDefinitionId}")
    @Override
    public DataRsp<ProcessDefinitionRsp> preview(@ApiParam(value = "流程定义id") @PathVariable String processDefinitionId) {
        return new DataRsp<>(processDefinitionService.preview(processDefinitionId));
    }

    @ApiOperation(value = "流程定义信息")
    @GetMapping("/find-by-procDefId/{procDefId}")
    @Override
    public DataRsp<ProcessDefinitionRsp> findByProcDefId(@ApiParam(value = "流程定义id") @PathVariable("procDefId") String procDefId) {
        ProcessDefinitionRsp processDefinition = processDefinitionService.findByProcDefId(procDefId);
        return new DataRsp(processDefinition);
    }

    @ApiOperation(value = "流程key下的流程定义信息")
    @GetMapping("/list-by-procDefKey/{procDefKey}")
    @Override
    public ListRsp<ProcessDefinitionRsp> listByProcDefKey(@PathVariable("procDefKey") String procDefKey) {
        List<ProcessDefinitionRsp> processDefinitions = processDefinitionService.findByProcDefKey(procDefKey);
        return new ListRsp<>(processDefinitions);
    }

    @PostMapping("/page/key")
    @ApiOperation(value = "流程key的分页")
    @Override
    public PageRsp<ProcessDefinitionRsp> pageKey(@RequestBody ProcessDefinitionSearchReq req) {
        PageImpl page = this.processDefinitionService.pageKey(req);
        return new PageRsp<>(req, page.getTotalElements(), page.getContent());
    }

    @PostMapping("/page/id")
    @ApiOperation(value = "流程id的分页")
    @Override
    public PageRsp<ProcessDefinitionRsp> pageId(@RequestBody ProcessDefinitionSearchReq req) {
        PageImpl<ProcessDefinitionRsp> page = this.processDefinitionService.pageId(req);
        return new PageRsp<>(req, page.getTotalElements(), page.getContent());
    }

    @ApiOperation(value = "流程发布流程定义分页")
    @PostMapping("/page/publish-process")
    @Override
    public PageRsp<ProcessDefinitionRsp> pagePublishProcess(@RequestBody ProcessDefinitionSearchReq req) {
        PageImpl page = this.processDefinitionService.pagePublishProcess(req);
        return new PageRsp<>(req, page.getTotalElements(), page.getContent());
    }

    @PostMapping("/upload")
    @ApiOperation(value = "上传流程图")
    @Override
    public DataRsp<ProcessDefinitionRsp> upload(ProcessDefinitionReq req, @RequestPart("file") MultipartFile file) {
        return new DataRsp<>(this.processDefinitionService.upload(req, file));
    }

    @PostMapping("/test/{processDefinitionId}")
    @ApiOperation(value = "测试流程,简单测试发起")
    @Override
    public CommonRsp test(@PathVariable String processDefinitionId, @RequestBody Map<String, Object> variables) {
        this.processDefinitionService.test(processDefinitionId, variables);
        return new CommonRsp();
    }

    @PostMapping("/on-off/{processDefinitionId}")
    @ApiOperation(value = "关停/启用")
    @Override
    public CommonRsp onOrOff(@PathVariable String processDefinitionId) {
        return this.processDefinitionService.updateState(processDefinitionId);
    }

    @PostMapping("/delete-in-procDefId")
    @ApiOperation(value = "删除流程定义")
    @Override
    public CommonRsp deleteByProcDefIdIn(@RequestBody List<String> procDefIds) {
        this.processDefinitionService.deleteByProcDefId(procDefIds);
        return new CommonRsp();
    }
}
