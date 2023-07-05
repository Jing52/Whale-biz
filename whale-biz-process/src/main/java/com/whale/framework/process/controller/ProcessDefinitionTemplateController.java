package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.dto.req.ProcessDefinitionSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.dto.req.ProcessDefinitionTemplateReq;
import com.whale.framework.process.dto.rsp.ProcessDefinitionTemplateRsp;
import com.whale.framework.process.entity.ProcessDefinition;
import com.whale.framework.process.entity.ProcessDefinitionTemplate;
import com.whale.framework.process.service.ProcessDefinitionTemplateService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.DataRsp;
import com.magus.framework.core.dto.rsp.PageRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/process/definition/template")
@Api(tags = "流程定义模板")
public class ProcessDefinitionTemplateController extends BaseController<ProcessDefinitionTemplate> {

	@Autowired
	private ProcessDefinitionTemplateService processDefinitionTemplateService;

	@PostMapping("/page")
	@ApiOperation(value = "流程定义信息列表")
	public PageRsp<ProcessDefinitionRsp> page(@RequestBody ProcessDefinitionSearchReq req) {
		PageImpl<ProcessDefinitionRsp> page = processDefinitionTemplateService.page(req);
		return new PageRsp<>(req, page.getTotalElements(), page.getContent());
	}

	@ApiOperation(value = "流程定义信息")
	@GetMapping("/{processDefinitionId}")
	public DataRsp<ProcessDefinitionTemplate> info(
			@ApiParam(value = "流程定义id") @PathVariable("processDefinitionId") String processDefinitionId) {
		ProcessDefinitionTemplate processDefinition = processDefinitionTemplateService.findByProcDefId(processDefinitionId);
		return new DataRsp(processDefinition);
	}

	@ApiOperation(value = "预览流程图")
	@GetMapping("/preview/{processDefinitionId}")
	public DataRsp<ProcessDefinitionTemplateRsp> preview(
			@ApiParam(value = "流程定义id") @PathVariable String processDefinitionId) {
		return new DataRsp<>(processDefinitionTemplateService.preview(processDefinitionId));
	}

	@PostMapping("/upload")
	@ApiOperation(value = "上传流程图")
	public DataRsp<ProcessDefinition> upload(@RequestBody ProcessDefinitionTemplateReq req,
                                             @RequestParam("file") MultipartFile file) {
		this.processDefinitionTemplateService.upload(req, file);
		return null;
	}

	@PostMapping("/delete/{processDefinitionId}")
	@ApiOperation(value = "删除流程流程定义")
	public CommonRsp delete(@PathVariable String processDefinitionId) {
		this.processDefinitionTemplateService.deleteById(processDefinitionId);
		return new CommonRsp();
	}

}
