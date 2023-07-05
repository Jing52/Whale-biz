package com.whale.framework.process.strategy.action.impl;

import com.whale.framework.process.annotation.ProcessActivity;
import com.magus.framework.camunda.api.dto.req.ProcessTaskActionReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.constants.OriginalSqlConstant;
import com.whale.framework.process.dto.ProcessIdentityLinkDTO;
import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.enums.ProcessActionEnum;
import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.service.ProcessDefinitionService;
import com.whale.framework.process.service.ProcessDelegateService;
import com.whale.framework.process.service.ProcessTaskService;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.datasource.DynamicDataSourceUtil;
import com.magus.framework.datasource.JdbcTemplateActuator;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.DutyRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.WorkgroupRestApi;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import com.magus.framework.system.api.dto.rsp.DutyRsp;
import com.magus.framework.system.api.dto.rsp.OrgDutyUserRsp;
import com.magus.framework.system.api.dto.rsp.WorkgroupRsp;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.strategy.impl.action
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/5 11:56 AM
 */
@Service
public class ProcessClaimStrategyImpl implements IProcessActionStrategy {

    @Autowired
    TaskService taskService;

    @Autowired
    DutyRestApi dutyRestApi;

    @Autowired
    WorkgroupRestApi workgroupRestApi;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Autowired
    ProcessTaskService processTaskService;

    @Autowired
    ProcessDelegateService processDelegateService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    JdbcTemplateActuator jdbcTemplate;


    @Override
    public String getStrategyKey() {
        return CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + ProcessActionEnum.ACTION_CLAIM.getCode();
    }

    /**
     * 认领
     *
     * @param req
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @ProcessActivity(action = ProcessActionEnum.ACTION_CLAIM)
    public void execute(ProcessTaskActionReq req) {
        log.info("=========================> 认领 入参：{}", JsonUtils.toJson(req));
        Task task = preExecute(req, taskService);

        boolean auth = checkAuth(req.getTaskId());
        if (auth) {
            String assignee = LoginUserUtils.getLoginId();
            // 认领
            taskService.claim(task.getId(), assignee);

            // 查询当前人是否存在委托,存在的认领后指定给委托人
            ProcessDefinitionRsp def = processDefinitionService.findByProcDefId(task.getProcessDefinitionId());
            if (Objects.isNull(def)) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
            }
            AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(def.getProcDefKey()).getData();
            if (Objects.isNull(sysGroup)) {
                throw new MagusException(ProcessResultEnum.RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST);
            }
            ProcessDelegate delegate = processDelegateService.getTargetDelegate(sysGroup.getId(), assignee);
            // 代理人
            String agent = delegate.getAssignee();

            if (StringUtils.isNotBlank(agent) && !StringUtils.equals(assignee, agent)) {
                log.info("=========================> 认领 存在委托任务:{}，委托人：{}, 代理人:{}", task.getId(), assignee, agent);
                // 委托
                taskService.delegateTask(task.getId(), agent);
            } else {
                log.info("=========================> 认领 防止节点同时设置了人和组，人委托了代理人，而当前用户在组内认领，无代理人,将代理状态置为NULL");
                // 防止节点同时设置了人和组，人委托了代理人，而当前用户在组内认领，无代理人,将代理状态置为NULL
                jdbcTemplate.updateToDb(OriginalSqlConstant.UPDATE_ACT_RU_TASK_1, DynamicDataSourceUtil.getDbName(), null, assignee, null, req.getTaskId());
            }

            // 认领后删除运行的候选组信息
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(req.getTaskId());
            for (IdentityLink identityLink : identityLinks) {
                if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                    taskService.deleteGroupIdentityLink(task.getId(), identityLink.getGroupId(), IdentityLinkType.CANDIDATE);
                }
            }
        }
        log.info("=========================> 认领 结束");
    }

    /**
     * 校验权限
     *
     * @param taskId
     * @return
     */
    private boolean checkAuth(String taskId) {
        log.info("=========================> 校验权限 入参：taskId：{}", taskId);
        boolean auth = Boolean.FALSE;
        List<ProcessIdentityLinkDTO> links = processTaskService.listGroupTaskByTaskId(Arrays.asList(taskId));
        if (CollectionUtils.isEmpty(links)) {
            return auth;
        }

        // 判断是否在认领的组织内
        String groupId = links.get(0).getGroupId();

        if (groupId.startsWith(ProcessAssigneeTypeEnum.$ORG.getPre())) {
            String[] groupIdstr = groupId.split("@");
            String orgId = groupIdstr[0].replace(ProcessAssigneeTypeEnum.$ORG.getPre(), "");
            String dutyId = null;
            if (groupIdstr.length == 2) {
                dutyId = groupIdstr[1];
            }
            List<OrgDutyUserRsp> orgs = userRestApi.listOrgDutyUser(orgId, dutyId, LoginUserUtils.getId()).getList();
            if (CollectionUtils.isNotEmpty(orgs)) {
                auth = Boolean.TRUE;
            }
        }

        if (groupId.startsWith(ProcessAssigneeTypeEnum.$WORKGROUP.getPre())) {
            String workgroupId = groupId.replace(ProcessAssigneeTypeEnum.$WORKGROUP.getPre(), "");
            List<WorkgroupRsp> workgroups = workgroupRestApi.listByIds(Arrays.asList(workgroupId)).getList();
            if (CollectionUtils.isNotEmpty(workgroups)) {
                auth = Boolean.TRUE;
            }
        }

        if (groupId.startsWith(ProcessAssigneeTypeEnum.$DUTY.getPre())) {
            String dutyId = groupId.replace(ProcessAssigneeTypeEnum.$DUTY.getPre(), "");

            List<DutyRsp> duties = dutyRestApi.listByIds(Arrays.asList(dutyId)).getList();
            if (CollectionUtils.isNotEmpty(duties)) {
                auth = Boolean.TRUE;
            }
        }
        return auth;
    }
}
