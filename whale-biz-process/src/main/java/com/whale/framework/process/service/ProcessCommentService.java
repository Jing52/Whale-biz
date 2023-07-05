package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.req.ProcessCommentReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessCommentRsp;
import com.whale.framework.process.entity.ProcessComment;
import com.whale.framework.process.repository.ProcessCommentRepository;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.service.BaseService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2022/12/8 10:58 AM
 */
@Service
public class ProcessCommentService extends BaseService<ProcessComment, String> {

    @Autowired
    ProcessCommentRepository repository;

    /**
     * 评论
     *
     * @param req
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createComment(ProcessCommentReq req) {
        if (StringUtils.isBlank(req.getComment()) || StringUtils.isBlank(req.getProcInstId())) {
            return;
        }

        ProcessComment comment = MagusUtils.copyProperties(req, ProcessComment.class);
        comment.setCreateUserName(LoginUserUtils.getUserName());
        repository.save(comment);
    }

    /**
     * 获取流程实例下的评论
     *
     * @param procInstId
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<ProcessCommentRsp> listByProcInstId(String procInstId) {
        if (StringUtils.isBlank(procInstId)) {
            return Lists.newArrayList();
        }
        List<ProcessComment> list = repository.findByProcInstIdAndDeletedIsFalseOrderByCreateTimeDesc(procInstId);

        List<ProcessCommentRsp> result = MagusUtils.copyList(list, ProcessCommentRsp.class);
        return result;
    }

    /**
     * 删除评论
     *
     * @param id
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        ProcessComment comment = findById(id);
        comment.setDeleted(Boolean.TRUE);
        save(comment);
    }
}
