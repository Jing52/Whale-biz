package com.whale.framework.process.strategy.action;

import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.strategy.base.BaseStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.LoginUserUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.base
 * @Description:
 * @Date: 2022/12/7 11:45 AM
 */
public interface IProcessActionStrategy extends BaseStrategy {

    Logger log = LoggerFactory.getLogger(IProcessActionStrategy.class);

    public final static String CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX = IProcessActionStrategy.class.getSimpleName() + KEY_CONCAT;

    void execute(ProcessTaskActionReq req);

    /**
     * 添加意见
     *
     * @param task
     * @param taskService
     * @param opinion
     */
    default void opinion(Task task, TaskService taskService, String opinion) {
        log.info("=========================> 添加意见 入参：taskId: {}, 意见：{}", task.getId(), opinion);
        // camunda创建意见
        if (StringUtils.isNotBlank(opinion)) {
            taskService.createComment(task.getId(), task.getProcessInstanceId(), opinion);
        }
    }

    default Task getTask(String taskId, TaskService taskService) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task;
    }

    default Task preExecute(ProcessTaskActionReq req, TaskService taskService) {
        if (Objects.isNull(req) || StringUtils.isBlank(req.getProcInstId()) || StringUtils.isBlank(req.getTaskId())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        String currentUserId = LoginUserUtils.getLoginId();

        Task task = getTask(req.getTaskId(), taskService);
        if (task == null) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_TASK_NOT_FOUND);
        }
        ProcessActionEnum action = ProcessActionEnum.valueOf(req.getAction());

        if (ProcessActionEnum.ACTION_CLAIM != action && !StringUtils.equals(currentUserId, task.getAssignee())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
        }
        switch (action) {
            case ACTION_COMPLETE -> {
                if (Objects.isNull(req.getTerminate())) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }

                if (!StringUtils.equals(currentUserId, task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
                }
            }
            case ACTION_DELEGATE -> {
                if (Objects.isNull(req.getDelegate()) || StringUtils.isBlank(req.getDelegate().getAssignee())) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }
                if (StringUtils.equals(currentUserId, req.getDelegate().getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_ASSIGNEE_ERROR);
                }
                if (!StringUtils.equalsAny(currentUserId, task.getOwner(), task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_ASSIGNEE_ERROR);
                }

                if (!StringUtils.equals(currentUserId, task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
                }
            }
            case ACTION_CARBON_COPY -> {
                if (Objects.isNull(req.getCc()) || StringUtils.isBlank(req.getCc().getCcId())) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }
                if (StringUtils.equals(currentUserId, req.getCc().getCcId())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_CC_ID_ERROR);
                }

                if (!StringUtils.equals(currentUserId, task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
                }
            }
            case ACTION_REJECT_FIRST -> {
                if (Objects.isNull(req.getTerminate())) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }

                if (!StringUtils.equals(currentUserId, task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
                }
            }
            case ACTION_CLAIM -> {

            }
            case ACTION_JUMP -> {
                if (Objects.isNull(req.getJump()) || StringUtils.isBlank(req.getJump().getTargetNodeKey())) {
                    throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
                }

                if (!StringUtils.equals(currentUserId, task.getAssignee())) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_OPERATOR_NOT_MATCH);
                }
            }
            case ACTION_REVERSE -> {

            }
            default -> log.error("current action is not exist!");
        }
        return task;
    }
}
