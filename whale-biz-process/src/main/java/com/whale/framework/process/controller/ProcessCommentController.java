package com.whale.framework.process.controller;

import com.magus.framework.camunda.api.ProcessCommentRestApi;
import com.magus.framework.camunda.api.dto.req.ProcessCommentReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessCommentRsp;
import com.whale.framework.process.entity.ProcessComment;
import com.whale.framework.process.service.ProcessCommentService;
import com.magus.framework.controller.BaseController;
import com.magus.framework.core.dto.rsp.CommonRsp;
import com.magus.framework.core.dto.rsp.ListRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.controller
 * @Description:
 * @Date: 2022/12/8 1:31 PM
 */
@Api(tags = "流程实例评论")
@RestController
@RequestMapping("/process/comment")
public class ProcessCommentController extends BaseController<ProcessComment> implements ProcessCommentRestApi {

    @Autowired
    ProcessCommentService processCommentService;


    /* ====================================对外暴露API接口========================================= */

    @GetMapping("/list-by-procInstId/{procInstId}")
    @ApiOperation(value = "获取评论")
    @Override
    public ListRsp<ProcessCommentRsp> listByProcInstId(@PathVariable("procInstId") String procInstId) {
        List<ProcessCommentRsp> list = processCommentService.listByProcInstId(procInstId);
        return new ListRsp<>(list);
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建评论")
    @Override
    public CommonRsp create(@RequestBody ProcessCommentReq comment) {
        processCommentService.createComment(comment);
        return new CommonRsp();
    }

    @PostMapping("/delete/{id}")
    @Override
    public CommonRsp delete(@PathVariable String id) {
        processCommentService.delete(id);
        return new CommonRsp();
    }
}
