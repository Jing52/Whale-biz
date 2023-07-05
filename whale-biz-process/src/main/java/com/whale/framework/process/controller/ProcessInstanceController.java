package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessInstanceRestApi;
import com.magus.framework.camunda.api.dto.req.ProcessApplyReq;
import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessCountRsp;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.whale.framework.process.entity.ProcessInstance;
import com.whale.framework.process.service.ProcessInstanceService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.DataRsp;
import com.magus.framework.core.dto.rsp.ListRsp;
import com.magus.framework.core.dto.rsp.PageRsp;
import com.magus.framework.core.utils.MagusUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process/instance")
@Api(tags = "流程实例")
public class ProcessInstanceController extends BaseController<ProcessInstance> implements ProcessInstanceRestApi {
	
	@Autowired
	private ProcessInstanceService processInstanceService;

	@GetMapping("/count")
	@ApiOperation(value = "流程统计）")
	@Override
	public DataRsp<ProcessCountRsp> count() {
		ProcessCountRsp rsp = processInstanceService.amount();
		return new DataRsp<>(rsp);
	}

	@PostMapping("/page")
	@ApiOperation(value = "分页查询")
	@Override
	public PageRsp<ProcessInstanceRsp> page(@RequestBody ProcessSearchReq req) {
		PageImpl<ProcessInstanceRsp> page = processInstanceService.page(req);
		return new PageRsp<>(req, page.getTotalElements(), page.getContent());
	}

	@GetMapping("/find-by-id/{id}")
	@ApiOperation(value = "详情")
	@Override
	public DataRsp<ProcessInstanceRsp> findById(@PathVariable("id") String id) {
		ProcessInstanceRsp rsp = processInstanceService.getProcessInfoById(id);
		return new DataRsp<>(rsp);
	}

	@GetMapping("/running/find-by-procDefKey/{procDefKey}")
	@ApiOperation(value = "查询流程key下启用的流程正在运行的流程实例")
	@Override
	public ListRsp<ProcessInstanceRsp> findRunningByProcDefKey(@PathVariable("procDefKey") String procDefKey) {
		List<ProcessInstanceRsp> rsp = processInstanceService.findRunningProcessInstance(procDefKey);
		return new ListRsp<>(rsp);
	}

	@ApiOperation(value = "根据ID查询详情")
	@GetMapping("/find-by-businessId")
	@Override
	public DataRsp<ProcessInstanceRsp> findByBusinessId(@RequestParam("businessId") String businessId) {
		ProcessInstanceRsp instance = processInstanceService.findByBusinessId(businessId);
		return new DataRsp<>(instance);
	}

	@ApiOperation(value = "根据ID查询")
	@PostMapping("list-in-businessId")
	@Override
	public ListRsp<ProcessInstanceRsp> listInBusinessId(@RequestBody List<String> businessIds) {
		List<ProcessInstanceRsp> instances = processInstanceService.listInBusinessId(businessIds);
		return new ListRsp<>(instances);
	}

	@PostMapping("/draft")
	@ApiOperation(value = "流程申请保存草稿")
	@Override
	public CommonRsp draft(@RequestBody ProcessApplyReq req) {
		processInstanceService.saveDraft(req);
		return new CommonRsp();
	}

	@PostMapping("/submit")
	@ApiOperation(value = "流程申请提交")
	@Override
	public CommonRsp submit(@RequestBody ProcessApplyReq req) {
		processInstanceService.submit(req);
		return new CommonRsp();
	}

	@PostMapping("/submit/{id}")
	@ApiOperation(value = "流程申请提交")
	@Override
	public CommonRsp submit(@PathVariable("id") String id, @RequestBody Map<String, Object> variables) {
		processInstanceService.submit(id, variables);
		return new CommonRsp();
	}

	@PostMapping("/update")
	@ApiOperation(value = "流程实例草稿编辑")
	@Override
	public CommonRsp update(@RequestBody ProcessApplyReq req) {
		processInstanceService.update(req);
		return new CommonRsp();
	}

	@PostMapping("/delete/{id}")
	@ApiOperation(value = "流程实例删除（待发&&本地）")
	@Override
	public CommonRsp delete(@PathVariable("id") String id) {
		processInstanceService.delete(id);
		return new CommonRsp();
	}

	@ApiOperation(value = "批量删除")
	@PostMapping("/delete-in-businessId")
	@Override
	public CommonRsp deleteByBusinessIdIn(@RequestBody List<String> ids) {
		processInstanceService.deleteByBusinessIdIn(ids);
		return new CommonRsp();
	}

}
