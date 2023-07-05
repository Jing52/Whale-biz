package com.whale.framework.process.repository;

import com.whale.framework.process.entity.ProcessComment;
import com.magus.framework.repository.BaseRepository;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.repository
 * @Description:
 * @Date: 2022/12/8 1:20 PM
 */
public interface ProcessCommentRepository extends BaseRepository<ProcessComment, String> {

    List<ProcessComment> findByProcInstIdAndDeletedIsFalseOrderByCreateTimeDesc(String procInstId);
}
