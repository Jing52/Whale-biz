package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.constants.OriginalSqlConstant;
import com.whale.framework.process.dto.req.ProcessDelegateReq;
import com.whale.framework.process.dto.req.ProcessDelegateSearchReq;
import com.whale.framework.process.dto.rsp.ProcessDelegateRsp;
import com.whale.framework.process.entity.ProcessDefinition;
import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.repository.ProcessDelegateRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.config.redis.MagusRedisClient;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.DateUtils;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.datasource.DynamicDataSourceUtil;
import com.magus.framework.datasource.JdbcTemplateActuator;
import com.magus.framework.generator.api.GeneratorApplicationRestApi;
import com.magus.framework.generator.api.GeneratorGroupRestApi;
import com.magus.framework.generator.api.dto.rsp.GeneratorApplicationRsp;
import com.magus.framework.generator.api.dto.rsp.GeneratorGroupRsp;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import com.magus.framework.service.BaseService;
import com.magus.framework.system.api.AppGroupRestApi;
import com.magus.framework.system.api.UserRestApi;
import com.magus.framework.system.api.dto.rsp.AppApplicationRsp;
import com.magus.framework.system.api.dto.rsp.AppGroupRsp;
import com.magus.framework.system.api.dto.rsp.TreeNodeRsp;
import com.magus.framework.system.api.dto.rsp.UserRsp;
import com.magus.framework.utils.PageUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description:
 * @Date: 2022/12/9 4:31 PM
 */
@Service
@Slf4j
public class ProcessDelegateService extends BaseService<ProcessDelegate, String> {

    public static final String DELEGATE_CONFIGURATION = "DC-";

    @Autowired
    ProcessDelegateRepository repository;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    GeneratorGroupRestApi generatorGroupRestApi;

    @Autowired
    GeneratorApplicationRestApi generatorApplicationRestApi;

    @Autowired
    TaskService taskService;

    @Autowired
    UserRestApi userRestApi;

    @Autowired
    AppGroupRestApi groupRestApi;

    @Autowired
    MagusRedisClient redisClient;

    @Autowired
    JdbcTemplateActuator jdbcTemplate;


    /**
     * 委托配置列表
     *
     * @param req
     * @return
     */
    public PageImpl<ProcessDelegateRsp> page(ProcessDelegateSearchReq req) {
        log.info("=========================> 委托配置列表 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req)) {
            return new PageImpl<>(Collections.emptyList(), PageUtils.page(req), 0);
        }
        // 拼装查询条件 定义基础nativeSQL
        StringBuilder queryBuilder = new StringBuilder("SELECT batch_no, state , max( create_time ) AS create_time FROM process_delegate where deleted = 0 and create_id = '").append(LoginUserUtils.getLoginId()).append("'");

        String countSql = "SELECT count(1) FROM (SELECT distinct u.batch_no FROM ( SELECT * FROM process_delegate %s ORDER BY create_time DESC, state DESC ) u GROUP BY u.batch_no, u.state) tmp";
        StringBuilder countCondition = new StringBuilder("where deleted = 0 and create_id = '").append(LoginUserUtils.getLoginId()).append("'");
        if (StringUtils.isNotBlank(req.getBatchNo())) {
            queryBuilder.append(" and batch_no = '").append(req.getBatchNo()).append("'");
            countCondition.append(" and batch_no = '").append(req.getBatchNo()).append("'");
        }
        if (StringUtils.isNotBlank(req.getSysGroupName())) {
            queryBuilder.append(" and sys_group_name like ").append("'%").append(req.getSysGroupName()).append("%'");
            countCondition.append(" and sys_group_name like ").append("'%").append(req.getSysGroupName()).append("%'");
        }
        // 添加GroupBy 条件
        queryBuilder.append(" group by batch_no, state order by create_time desc, state desc ");
        queryBuilder.append(" limit ").append(req.getStartNum()).append(",").append(req.getPageSize());

        countSql = String.format(countSql, countCondition);
        log.info("=========================> 委托配置列表 查询sql：{}", queryBuilder);
        log.info("=========================> 委托配置列表 统计sql：{}", countSql);
        // 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForListToDb(queryBuilder.toString(), DynamicDataSourceUtil.getDbName());
        Integer total = jdbcTemplate.queryForObjectToDb(countSql, DynamicDataSourceUtil.getDbName(), Integer.class);

        List<String> batchNos = resultList.stream().map(map -> map.get("batch_no").toString()).collect(Collectors.toList());
        log.info("=========================> 委托配置列表 批次号：{}", JsonUtils.toJson(batchNos));

        // 根据批次号查询所有的委托实例
        List<ProcessDelegate> delegateList = repository.findByBatchNoInAndDeletedIsFalse(batchNos);
        log.info("=========================> 委托配置列表 委托配置：{}", JsonUtils.toJson(delegateList));
        Map<String, List<ProcessDelegate>> delegateMap = delegateList.stream().collect(Collectors.groupingBy(ProcessDelegate::getBatchNo));

        // 查询受让人信息
        List<String> loginIds = delegateList.stream().map(ProcessDelegate::getAssignee).collect(Collectors.toList());
        List<UserRsp> users = userRestApi.listUsersByLoginId(loginIds).getList();
        Map<String, UserRsp> userMap = users.stream().collect(Collectors.toMap(UserRsp::getLoginId, Function.identity(), (x, y) -> x));

        // 封装返回结果
        List<ProcessDelegateRsp> res = batchNos.stream().map(batchNo -> {
            // 查询应用
            List<ProcessDelegate> batchDelegates = Optional.ofNullable(delegateMap.get(batchNo)).orElse(new ArrayList<>());
            List<String> sysGroupIds = batchDelegates.stream().map(ProcessDelegate::getSysGroupId).collect(Collectors.toList());

            List<AppApplicationRsp> sysGroups = groupRestApi.findAppGroup(sysGroupIds).getList();
            // 封装参数
            UserRsp assigneeUser = Optional.ofNullable(userMap.get(batchDelegates.get(0).getAssignee())).orElse(new UserRsp());
            ProcessDelegateRsp delegatePageRsp = ProcessDelegateRsp.builder()
                    .batchNo(batchNo)
                    .assignee(assigneeUser.getLoginId())
                    .assigneeName(assigneeUser.getUserName())
                    .sysApps(sysGroups)
                    .startTime(batchDelegates.get(0).getStartTime())
                    .endTime(batchDelegates.get(0).getEndTime())
                    .state(batchDelegates.get(0).getState())
                    .remark(batchDelegates.get(0).getRemark())
                    .createTime(batchDelegates.get(0).getCreateTime())
                    .build();
            return delegatePageRsp;
        }).collect(Collectors.toList());
        log.info("=========================> 委托配置列表 结果：{}", JsonUtils.toJson(res));

        return new PageImpl<>(res, PageUtils.page(req), total);
    }

    /**
     * 委托配置详情
     *
     * @param batchNo
     * @return
     */
    public ProcessDelegateRsp info(String batchNo) {
        log.info("=========================> 委托配置详情 入参：{}", batchNo);
        if (StringUtils.isBlank(batchNo)) {
            return new ProcessDelegateRsp();
        }
        // 根据批次查询到多条委托实例
        List<ProcessDelegate> processDelegates = repository.findByBatchNoAndDeletedIsFalse(batchNo);
        if (CollectionUtils.isEmpty(processDelegates)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_ERROR);
        }

        // 查询应用
        List<String> sysGroupIds = processDelegates.stream().map(ProcessDelegate::getSysGroupId).distinct().collect(Collectors.toList());
        List<AppApplicationRsp> sysGroups = groupRestApi.findAppGroup(sysGroupIds).getList();

        // 封装返回结果
        ProcessDelegate delegate = processDelegates.get(0);
        UserRsp userRsp = userRestApi.findByLoginId(delegate.getAssignee()).getData();
        ProcessDelegateRsp result = new ProcessDelegateRsp();
        result.setSysApps(sysGroups);
        result.setSysGroupIds(sysGroupIds);
        result.setAssignee(delegate.getAssignee());
        result.setAssigneeName(userRsp.getUserName());
        result.setBatchNo(batchNo);
        result.setStartTime(delegate.getStartTime());
        result.setEndTime(delegate.getEndTime());
        result.setRemark(delegate.getRemark());
        result.setState(delegate.getState());
        log.info("=========================> 委托配置详情 结果：{}", JsonUtils.toJson(result));
        return result;
    }

    /**
     * 委托配置新增
     *
     * @param req
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(ProcessDelegateReq req) {
        log.info("=========================> 委托配置新增 入参：{}", JsonUtils.toJson(req));
        if (Objects.isNull(req) || req.checkParam()) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }

        if (StringUtils.equals(LoginUserUtils.getLoginId(), req.getAssignee())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_FAILED);
        }

        List<String> sysGroupIds = req.getSysGroupIds();

        List<ProcessDelegate> groupDelegates = repository.findBySysGroupIdInAndCreateIdAndStateIsTrueAndDeletedIsFalse(sysGroupIds, LoginUserUtils.getLoginId());
        log.info("=========================> 委托配置新增 开始校验委托时间：{}", JsonUtils.toJson(groupDelegates));
        for (ProcessDelegate groupDelegate : groupDelegates) {
            Date startTime = groupDelegate.getStartTime();
            Date endTime = groupDelegate.getEndTime();
            if (Objects.nonNull(endTime)) {
                if (Objects.isNull(req.getEndTime())) {
                    if (req.getStartTime().compareTo(endTime) <= 0) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_TIME_OVERLAP);
                    }
                } else {
                    if (req.getStartTime().compareTo(startTime) >= 0 && req.getStartTime().compareTo(endTime) <= 0) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_TIME_OVERLAP);
                    }
                    if (req.getEndTime().compareTo(startTime) >= 0 && req.getEndTime().compareTo(endTime) <= 0) {
                        throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_TIME_OVERLAP);
                    }
                }
            } else {
                if (Objects.isNull(req.getEndTime())
                        || (Objects.nonNull(req.getEndTime()) && startTime.compareTo((req.getEndTime())) <= 0)) {
                    throw new MagusException(ProcessResultEnum.RESULT_ERROR_DELEGATE_TIME_OVERLAP);
                }
            }
        }

        // 委托配置允许保存，落地
        List<AppGroupRsp> sysGroups = groupRestApi.listBySysGroupId(sysGroupIds).getList();
        Map<String, AppGroupRsp> groupMap = sysGroups.stream().collect(Collectors.toMap(AppGroupRsp::getId, Function.identity(), (x, y) -> x));
        String batchNo = DELEGATE_CONFIGURATION + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) + "-" + UUID.randomUUID().toString().split("-")[0];
        List<ProcessDelegate> processDelegates = sysGroupIds.stream().map(data -> {
            AppGroupRsp sysGroup = Optional.ofNullable(groupMap.get(data)).orElse(new AppGroupRsp());
            ProcessDelegate delegate = new ProcessDelegate();
            BeanUtils.copyProperties(req, delegate);
            delegate.setSysGroupId(data);
            delegate.setSysGroupName(sysGroup.getName());
            delegate.setSysAppId(sysGroup.getAppId());
            delegate.setBatchNo(batchNo);
            delegate.setOwner(LoginUserUtils.getLoginId());
            delegate.setState(Boolean.TRUE);
            return delegate;
        }).collect(Collectors.toList());
        log.info("=========================> 委托配置新增 待保存：{}", JsonUtils.toJson(processDelegates));
        repository.saveAll(processDelegates);

        // 委托
        changeDelegate(batchNo, req.getStartTime(), req.getEndTime(), req.getAssignee(), sysGroupIds);
    }

    /**
     * 更新委托配置
     *
     * @param req
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void update(ProcessDelegate req) {
        if (Objects.isNull(req) || StringUtils.isBlank(req.getBatchNo())
                || Objects.isNull(req.getStartTime()) || (Objects.nonNull(req.getEndTime()) && req.getStartTime().after(req.getEndTime()))) {
            return;
        }
        List<ProcessDelegate> delegates = repository.findByBatchNoAndStateIsTrueAndDeletedIsFalse(req.getBatchNo());

        if (CollectionUtils.isNotEmpty(delegates)) {
            delegates.forEach(x -> {
                x.setStartTime(req.getStartTime());
                x.setEndTime(req.getEndTime());
            });
            repository.saveAll(delegates);

            // 委托
            List<String> groupIds = delegates.stream().map(ProcessDelegate::getSysGroupId).collect(Collectors.toList());
            changeDelegate(req.getBatchNo(), req.getStartTime(), req.getEndTime(), delegates.get(0).getAssignee(), groupIds);
        }
    }

    /**
     * 委托状态修改
     *
     * @param req
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateDelegateState(ProcessDelegateReq req) {
        log.info("=========================> 委托状态修改 入参: {} ", JsonUtils.toJson(req));
        if (Objects.isNull(req) || StringUtils.isBlank(req.getBatchNo()) || req.getState() == null) {
            return;
        }
        List<ProcessDelegate> delegates = repository.findByBatchNoAndDeletedIsFalse(req.getBatchNo());
        delegates.forEach(x -> {
            x.setState(req.getState());
        });
        repository.saveAll(delegates);

        if (BooleanUtils.isFalse(req.getState())) {
            // 取消委托
            List<String> groupIds = delegates.stream().map(ProcessDelegate::getSysGroupId).collect(Collectors.toList());
            ProcessDelegate processDelegate = delegates.get(0);
            List<AppGroupRsp> appGroups = groupRestApi.listBySysGroupId(groupIds).getList();
            List<String> procDefKeys = appGroups.stream().map(AppGroupRsp::getProcDefKey).collect(toList());
            List<ProcessDefinition> defs = processDefinitionService.findUpDefinitionByKeyIn(procDefKeys);
            cancelDelegate(processDelegate.getOwner(), processDelegate.getAssignee(), defs);

            // 取消委托缓存
            redisClient.deleteObject(req.getBatchNo());
        }
    }

    /**
     * 获取目标委托人
     *
     * @param sysGroupId      流程定义
     * @param currentAssignee 当前操作人
     * @return
     */
    public ProcessDelegate getTargetDelegate(String sysGroupId, String currentAssignee) {
        // 设置边界， 当前处理人为空，截止
        if (StringUtils.isBlank(sysGroupId) || StringUtils.isBlank(currentAssignee)) {
            return new ProcessDelegate();
        }
        List<SearchFilter> orFilters = new ArrayList<>();
        orFilters.add(SearchFilter.ge(ProcessDelegate::getEndTime, new Date()));
        orFilters.add(SearchFilter.isNull(ProcessDelegate::getEndTime));

        Specification<ProcessDelegate> orSpec = JpaSearchUtils.buildOrSpec(orFilters);

        List<SearchFilter> andFilters = new ArrayList<>();
        andFilters.add(SearchFilter.equal(ProcessDelegate::getDeleted, 0));
        andFilters.add(SearchFilter.le(ProcessDelegate::getStartTime, new Date()));
        andFilters.add(SearchFilter.equal(ProcessDelegate::getSysGroupId, sysGroupId));
        andFilters.add(SearchFilter.equal(ProcessDelegate::getState, 1));
        andFilters.add(SearchFilter.equal(ProcessDelegate::getOwner, currentAssignee));

        Specification<ProcessDelegate> andSpec = JpaSearchUtils.buildAndSpec(andFilters);

        List<ProcessDelegate> delegates = this.repository.findAll(JpaSearchUtils.and(orSpec, andSpec));

        if (CollectionUtils.isNotEmpty(delegates)) {
            return delegates.get(0);
        }
        return new ProcessDelegate();
    }

    /**
     * 变更委托 新增编辑委托配置时，当前时间在开始结束时间期间，找到当前用户正在运行的任务，委托给指定的人,否则，取消委托
     *
     * @param batchNo
     * @param startTime
     * @param endTime
     * @param assignee
     * @param sysGroupIds
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    private void changeDelegate(String batchNo, Date startTime, Date endTime, String assignee, List<String> sysGroupIds) {
        log.info("=========================> 变更委托 ");
        // 找到当前用户正在运行的任务，委托给指定的人
        List<ProcessDefinition> defs = processDefinitionService.listUpDefinitionBySysGroupId(sysGroupIds);
        if (CollectionUtils.isNotEmpty(defs)) {
            if (startTime.before(new Date()) && (Objects.isNull(endTime) || (Objects.nonNull(endTime) && endTime.after(new Date())))) {
                log.info("=========================> 变更委托 新增委托 ");
                // 当前时间在开始结束时间期间 委托
                delegate(LoginUserUtils.getLoginId(), assignee, defs);

                // 新增委托缓存
                if (Objects.nonNull(endTime)) {
                    LocalDateTime localDateTime = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    int seconds = (int) DateUtils.getSecondsToTargetTime(localDateTime);
                    redisClient.setCacheObject(batchNo, endTime, seconds, TimeUnit.SECONDS);
                } else {
                    redisClient.setCacheObject(batchNo, endTime);
                }
            } else {
                log.info("=========================> 变更委托 取消委托 ");
                // 不在当前时间在开始结束时间期间 取消委托
                cancelDelegate(LoginUserUtils.getLoginId(), assignee, defs);
                // 删除key
                redisClient.deleteObject(batchNo);
            }
        }
    }


    /**
     * 委托（根据用户任务委托）
     *
     * @param task
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delegate(Task task) {
        // 获取下一个节点的处理人
        String currentAssignee = task.getAssignee();
        if (StringUtils.isBlank(currentAssignee)) {
            return;
        }
        // 查询最终委托人
        ProcessDefinitionRsp def = processDefinitionService.findByProcDefId(task.getProcessDefinitionId());
        if (Objects.isNull(def)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }
        AppGroupRsp sysGroup = groupRestApi.findByProcDefKey(def.getProcDefKey()).getData();
        if (Objects.isNull(sysGroup)) {
            return;
        }
        ProcessDelegate delegate = getTargetDelegate(sysGroup.getId(), currentAssignee);
        String assignee = delegate.getAssignee();

        if (StringUtils.isNotBlank(assignee)) {
            log.info("委托任务:{},目标用户:{}", task.getId(), assignee);
            taskService.delegateTask(task.getId(), assignee);
        }
    }

    /**
     * 委托（根据流程定义实现委托）
     *
     * @param assignee
     * @param defs
     */
    private void delegate(String owner, String assignee, List<ProcessDefinition> defs) {
        log.info("=========================> 委托（根据流程定义实现委托）owner：{},  assignee: {}, defs: {}", owner, assignee, JsonUtils.toJson(defs));
        // 根据processDefinitionKey批量查询，无processDefinitionIdIn方法
        List<String> procDefKeys = defs.stream().map(ProcessDefinition::getProcDefKey).distinct().collect(Collectors.toList());
        List<String> procDefIds = defs.stream().map(ProcessDefinition::getProcDefId).distinct().collect(Collectors.toList());
        // 当前时间在开始结束时间期间 委托
        List<Task> allTasks = taskService.createTaskQuery().taskIdIn().processDefinitionKeyIn(procDefKeys.toArray(new String[procDefKeys.size()])).taskAssignee(owner).list();
        // 过滤出符合条件的任务进行委托
        List<Task> tasks = allTasks.stream().filter(task -> procDefIds.contains(task.getProcessDefinitionId())).collect(Collectors.toList());
        for (Task task : tasks) {
            taskService.delegateTask(task.getId(), assignee);
        }
    }

    /**
     * 取消委托（根据流程定义取消委托）
     *
     * @param owner
     * @param assignee
     * @param defs
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelDelegate(String owner, String assignee, List<ProcessDefinition> defs) {
        log.info("=========================> 取消委托（根据流程定义取消委托）owner：{},  assignee: {}, defs: {}", owner, assignee, JsonUtils.toJson(defs));
        // 根据processDefinitionKey批量查询，无processDefinitionIdIn方法
        List<String> procDefKeys = defs.stream().map(ProcessDefinition::getProcDefKey).distinct().collect(Collectors.toList());
        List<String> procDefIds = defs.stream().map(ProcessDefinition::getProcDefId).distinct().collect(Collectors.toList());
        // 获取对应流程，处理人为assignee,owner为登录人，并且是委托状态的，取消委托
        List<Task> allTasks = taskService.createTaskQuery().taskIdIn().processDefinitionKeyIn(procDefKeys.toArray(new String[procDefKeys.size()])).taskOwner(owner).taskAssignee(assignee).taskDelegationState(DelegationState.PENDING).list();
        // 过滤出符合条件的任务进行取消委托
        List<Task> tasks = allTasks.stream().filter(task -> procDefIds.contains(task.getProcessDefinitionId())).collect(Collectors.toList());
        for (Task task : tasks) {
            //防止节点同时设置了人和组，人委托了代理人，而当前用户在组内认领，无代理人,将代理状态置为NULL
            jdbcTemplate.updateToDb(OriginalSqlConstant.UPDATE_ACT_RU_TASK_1, DynamicDataSourceUtil.getDbName(), null, owner, null, task.getId());
        }
    }

    /**
     * 取消委托（根据委托配置取消委托）
     *
     * @param delegates
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelDelegate(List<ProcessDelegate> delegates) {
        log.info("=========================> 取消委托（根据委托配置取消委托）delegates: {}", JsonUtils.toJson(delegates));
        if (CollectionUtils.isEmpty(delegates)) {
            return;
        }
        // 委托配置失效
        delegates.forEach(x -> {
            x.setState(Boolean.FALSE);
        });
        repository.saveAll(delegates);

        List<String> sysGroupIds = delegates.stream().map(ProcessDelegate::getSysGroupId).collect(Collectors.toList());
        String owner = delegates.get(0).getOwner();
        String assignee = delegates.get(0).getAssignee();
        List<ProcessDefinition> defs = processDefinitionService.listUpDefinitionBySysGroupId(sysGroupIds);

        // 委托任务取消
        cancelDelegate(owner, assignee, defs);
    }

    /**
     * 对应的批次号委托是否失效
     *
     * @param batchNo
     * @return
     */
    public List<ProcessDelegate> expiredByBatchNo(String batchNo) {
        if (StringUtils.isBlank(batchNo)) {
            return null;
        }
        List<ProcessDelegate> delegates = repository.findByBatchNoAndStateIsTrueAndDeletedIsFalse(batchNo);
        return delegates;
    }

    /**
     * 查找所有已经过期但是状态未失效的委托配置
     *
     * @return
     */
    public List<ProcessDelegate> findExpiredDelegate() {
        return repository.findByEndTimeLessThanAndStateAndDeletedIsFalse(new Date(), 1);
    }

    /**
     * 查询未被委托的功能组树结构
     *
     * @return
     */
    public List<TreeNodeRsp> treeDelegateGroup() {
        List<GeneratorGroupRsp> groups = generatorGroupRestApi.findOnlineGroup().getList();
        List<String> appIds = groups.stream().map(GeneratorGroupRsp::getAppId).collect(Collectors.toList());
        List<GeneratorApplicationRsp> applications = generatorApplicationRestApi.list(appIds).getList();
        Set<String> sysAppIds = applications.stream().map(GeneratorApplicationRsp::getSysAppId).collect(Collectors.toSet());
        Set<String> sysGroupIds = groups.stream().map(GeneratorGroupRsp::getSysGroupId).collect(Collectors.toSet());
        List<TreeNodeRsp> treeNodes = groupRestApi.delegateTree().getList();
        List<TreeNodeRsp> result = new ArrayList<>();
        for (TreeNodeRsp treeNode : treeNodes) {
            if (!sysAppIds.contains(treeNode.getId())) {
                continue;
            }
            TreeNodeRsp node = handlerOnOff(treeNode, sysGroupIds);
            if (Objects.nonNull(node)) {
                result.add(node);
            }
        }
        return result;
    }

    private TreeNodeRsp handlerOnOff(TreeNodeRsp treeNode, Set<String> sysGroupIds) {
        if (sysGroupIds.contains(treeNode.getId())) {
            Map<String, Object> map = new HashMap<>();
            map.put(treeNode.getId(), Boolean.TRUE);
            treeNode.setExtend(map);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put(treeNode.getId(), Boolean.FALSE);
            treeNode.setExtend(map);
        }
        List<TreeNodeRsp> children = treeNode.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            for (TreeNodeRsp child : children) {
                handlerOnOff(child, sysGroupIds);
            }
        }
        return treeNode;
    }
}
