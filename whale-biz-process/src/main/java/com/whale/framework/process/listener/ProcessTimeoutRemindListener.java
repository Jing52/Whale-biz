package com.whale.framework.process.listener;//package com.magus.framework.camunda.listener;
//
//import com.magus.framework.message.api.MailRestApi;
//import com.magus.framework.message.api.dto.req.MailReq;
//import com.magus.framework.system.api.UserRestApi;
//import com.magus.framework.system.api.dto.rsp.UserRsp;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.camunda.bpm.engine.TaskService;
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//import org.camunda.bpm.engine.delegate.JavaDelegate;
//import org.camunda.bpm.engine.task.IdentityLink;
//import org.camunda.bpm.engine.task.IdentityLinkType;
//import org.camunda.bpm.engine.task.Task;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author Whale
// * @ProjectName: magus-engine
// * @See: com.magus.framework.camunda.listener
// * @Description:
// * @Date: 2023/5/23 4:42 PM
// */
//@Slf4j
//@Service("processTimeoutRemindListener")
//public class ProcessTimeoutRemindListener implements JavaDelegate {
//
//    private final static String SUBJECT = "";
//
//    private final static String CONTEXT = "%s 于 %s 提交的申请已超时, 请尽快审批！";
//
//    @Resource
//    private TaskService taskService;
//
//    @Resource
//    private MailRestApi mailRestApi;
//
//    @Resource
//    private UserRestApi userRestApi;
//
//    @Override
//    public void execute(DelegateExecution execution) throws Exception {
//        log.info("TimeoutRemindDelegate.execute start, execution: {}", execution);
//
//        String currentActivityId = execution.getCurrentActivityId();
//        // 获取当前节点的所有待办任务
//        List<Task> tasks = taskService.createTaskQuery()
//                .processInstanceId(execution.getProcessInstanceId())
//                .taskDefinitionKey(currentActivityId)
//                .list();
//
//        List<String> to = new ArrayList<>();
//        List<String> cc = new ArrayList<>();
//        // 发送人
//        List<String> assignees = tasks.stream().map(Task::getAssignee).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(assignees)) {
//            List<UserRsp> assigneeUsers = userRestApi.listUsersByUserId(assignees).getList();
//            to = assigneeUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//        }
//
//        // 抄送人
//        List<String> owners = tasks.stream().map(Task::getOwner).collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(owners)) {
//            List<UserRsp> ownerUsers = userRestApi.listUsersByUserId(owners).getList();
//            cc = ownerUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//        }
//
//        MailReq req = new MailReq();
//        req.setSubject(SUBJECT);
//        req.setContext(CONTEXT);
//
//        // 发送邮件
//        if (CollectionUtils.isEmpty(to)) {
//            // 代理组
//            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(execution.getId());
//            List<String> groupIds = identityLinks.stream()
//                    .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()) && link.getGroupId() != null)
//                    .map(IdentityLink::getGroupId).collect(Collectors.toList());
//            List<UserRsp> assigneeUsers = userRestApi.listAssignee(groupIds).getList();
//            if (CollectionUtils.isNotEmpty(assigneeUsers)) {
//                to = assigneeUsers.stream().map(UserRsp::getEmail).collect(Collectors.toList());
//            }
//        }
//
//        req.setTo(to.toArray(new String[0]));
//        req.setCc(cc.toArray(new String[0]));
//        mailRestApi.sendMail(req);
//
//        log.info("TimeoutRemindDelegate.execute end");
//    }
//}
