package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessNodeRestApi;
import com.magus.framework.camunda.api.dto.rsp.ProcessNodeRsp;
import com.whale.framework.process.entity.ProcessNode;
import com.whale.framework.process.service.ProcessNodeService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.ListRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/process/node")
@Api(tags = "流程节点定义")
public class ProcessNodeController extends BaseController<ProcessNode> implements ProcessNodeRestApi {
	
	@Autowired
	private ProcessNodeService processNodeService;

	@GetMapping("/not-run/list-by-taskId/{taskId}")
	@ApiOperation(value = "获取流程后续还未执行的节点")
	public ListRsp<ProcessNodeRsp> listNotRunningAct(@PathVariable String taskId) {
		return new ListRsp<>(this.processNodeService.listNotRunningAct(taskId));
	}



	/* ====================================对外暴露API接口========================================= */

	@GetMapping("/list-by-procDefId/{procDefId}")
	@Override
	public ListRsp<ProcessNodeRsp> listByProcDefId(@PathVariable String procDefId) {
		List<ProcessNodeRsp> nodes = processNodeService.findByProcDefId(procDefId);
		return new ListRsp<>(nodes);
	}
}
