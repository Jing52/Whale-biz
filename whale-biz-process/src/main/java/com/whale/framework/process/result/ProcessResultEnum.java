package com.whale.framework.process.result;

import com.magus.framework.core.exception.ResultCode;
import com.magus.framework.core.exception.ResultEnumBase;
import com.magus.framework.core.exception.ResultEnumObject;

/**
 * @author: xiaochuan.xiong
 * @date: 2022年12月7日
 * @notes:
 */
public enum ProcessResultEnum implements ResultEnumBase {


    /**
     * 流程定义不存在
     */
    RESULT_ERROR_PROC_DEF_NOT_EXIST(40_000_001, ResultCode.FAIL_BUSINESS, "proc.error.def.not.exist"),

    /**
     * 未找到策略
     */
    RESULT_ERROR_STRATEGY_NOT_FOUND(40_000_002, ResultCode.FAIL_BUSINESS, "proc.error.strategy.not.found"),

    /**
     * 节点操作人和登录人不匹配
     */
    RESULT_ERROR_OPERATOR_NOT_MATCH(40_000_003, ResultCode.FAIL_BUSINESS, "proc.error.assignee.operator.not.match"),

    /**
     * 流程图设计错误
     */
    RESULT_ERROR_BPMN_DESIGN(40_000_004, ResultCode.FAIL_BUSINESS, "proc.error.bpmn.design.is.error"),

    /**
     * 非当前任务办理人,不可委托办理
     */
    RESULT_ERROR_DELEGATE_ERROR(40_000_005, ResultCode.FAIL_BUSINESS, "proc.error.delegate.error"),

    /**
     * 委托人异常
     */
    RESULT_ERROR_ASSIGNEE_ERROR(40_000_006, ResultCode.FAIL_BUSINESS, "proc.error.assignee.error"),

    /**
     * 禁止传阅本人
     */
    RESULT_ERROR_CC_ID_ERROR(40_000_007, ResultCode.FAIL_BUSINESS, "proc.error.cc.person.error"),

    /**
     * 流程图解析失败
     */
    RESULT_ERROR_PROCESS_PARSE(40_000_008, ResultCode.FAIL_BUSINESS, "proc.error.parse.failed"),

    /**
     * 已存在激活的流程图
     */
    RESULT_ERROR_CLOSE_UP_PROCESS(40_000_009, ResultCode.FAIL_BUSINESS, "proc.error.close.up.process"),

    /**
     * 流程实例不存在
     */
    RESULT_ERROR_PROC_INST_NOT_FOUND(40_000_010, ResultCode.FAIL_BUSINESS, "proc.error.proc.inst.not.found"),

    /**
     * 不存在的应用
     */
    RESULT_ERROR_APP_NOT_EXIST(40_000_011, ResultCode.FAIL_BUSINESS, "proc.error.app.not.exist"),

    /**
     * 不存在的功能组
     */
    RESULT_ERROR_GROUP_NOT_EXIST(40_000_012, ResultCode.FAIL_BUSINESS, "proc.error.group.not.exist"),

    /**
     * 任务不存在
     */
    RESULT_ERROR_PROC_TASK_NOT_FOUND(40_000_013, ResultCode.FAIL_BUSINESS, "proc.error.task.not.exist"),

    /**
     * 不存在数据
     */
    RESULT_ERROR_DATA_NOT_EXIST(40_000_014, ResultCode.FAIL_BUSINESS, "proc.error.data.not.exist"),

    /**
     * 委托失败
     */
    RESULT_ERROR_DELEGATE_FAILED(40_000_015, ResultCode.FAIL_BUSINESS, "proc.error.delegate.failed"),

    /**
     * 功能组页面未设置首页
     */
    RESULT_ERROR_GROUP_PAGE_NO_INDEX(40_000_016, ResultCode.FAIL_BUSINESS, "proc.error.group.page.no.index"),

    /**
     * 动态活动不存在
     */
    RESULT_ERROR_ACTIVITY_NOT_EXIST(40_000_017, ResultCode.FAIL_BUSINESS, "proc.error.dynamic.activity.not.exist"),

    /**
     * 节点定义不存在
     */
    RESULT_ERROR_NODE_NOT_EXIST(40_000_018, ResultCode.FAIL_BUSINESS, "proc.error.node.not.exist"),

    /**
     * 流程运行中
     */
    RESULT_ERROR_INSTANCE_IS_PENDING(40_000_019, ResultCode.FAIL_BUSINESS, "proc.error.instance.is.running"),

    /**
     * 已存在流程实例，无法删除
     */
    RESULT_ERROR_PROC_INST_EXIST(40_000_020, ResultCode.FAIL_BUSINESS, "proc.error.process.instance.exist"),

    /**
     * 非登录人无法操作
     */
    RESULT_ERROR_UNABLE_OPERATE(40_000_021, ResultCode.FAIL_BUSINESS, "proc.error.non-logged-in.person.cannot.operate"),

    /**
     * 存在历史运行记录
     */
    RESULT_ERROR_HISTORY_PENDING_RECORD(40_000_022, ResultCode.FAIL_BUSINESS, "proc.error.history.pending.record"),

    /**
     * 存在历史运行记录
     */
    RESULT_ERROR_PROCESS_DEFINITION_KEY_EXIST(40_000_023, ResultCode.FAIL_BUSINESS, "proc.error.proc.def.key.is.exist"),

    /**
     * 不存在的系统功能组
     */
    RESULT_ERROR_SYSTEM_GROUP_NOT_EXIST(40_000_024, ResultCode.FAIL_BUSINESS, "proc.error.system.group.not.exist"),

    /**
     * 不存在的系统应用
     */
    RESULT_ERROR_SYSTEM_APPLICATION_NOT_EXIST(40_000_025, ResultCode.FAIL_BUSINESS, "proc.error.system.application.not.exist"),

    /**
     * code已存在
     */
    RESULT_ERROR_CODE_EXIST(40_000_026, ResultCode.FAIL_BUSINESS, "proc.error.code.exist"),

    /**
     * 流程已启用，无法删除
     */
    RESULT_ERROR_PROC_DEF_UP(40_000_027, ResultCode.FAIL_BUSINESS, "proc.error.process.definition.up"),

    /**
     * 代理人也是当前节点的处理人之一,无法委托
     */
    RESULT_ERROR_DELEGATE_IS_ASSIGNEE(40_000_028, ResultCode.FAIL_BUSINESS, "proc.error.delegate.is.assignee"),

    /**
     * 委托的系统功能组时间冲突
     */
    RESULT_ERROR_DELEGATE_TIME_OVERLAP(40_000_029, ResultCode.FAIL_BUSINESS, "proc.error.delegate.time.overlap"),

    /**
     * 代理人无法委托
     */
    RESULT_ERROR_AGENT_UNABLE_ENTRUST(40_000_030, ResultCode.FAIL_BUSINESS, "proc.error.agent.unable.entrust"),

    /**
     * 代理人无法跳转
     */
    RESULT_ERROR_UNABLE_JUMP(40_000_033, ResultCode.FAIL_BUSINESS, "proc.error.agent.unable.jump"),

    /**
     * 代理人无法跳转
     */
    RESULT_ERROR_UNABLE_CC(40_000_034, ResultCode.FAIL_BUSINESS, "proc.error.agent.unable.cc"),

    /**
     * 用户不存在
     */
    RESULT_ERROR_USER_NOT_EXIST(40_000_035, ResultCode.FAIL_BUSINESS, "proc.error.user.not.exist"),

    /**
     * 申请人不存在
     */
    RESULT_ERROR_APPLICANT_NOT_EXIST(40_000_036, ResultCode.FAIL_BUSINESS, "proc.error.applicant.not.exist"),

    /**
     * 业务数据已存在
     */
    RESULT_ERROR_BUSINESS_DATA_EXIST(40_000_037, ResultCode.FAIL_BUSINESS, "proc.error.business.data.exist"),
    ;
    private ResultEnumObject bizResultEnumObject;// 枚举值对象

    private ProcessResultEnum(Integer bizCode, ResultCode resultCode, String message) {
        bizResultEnumObject = new ResultEnumObject(bizCode, resultCode, message);
    }

    @Override
    public ResultEnumObject getBizResultEnumObject() {
        return this.bizResultEnumObject;
    }

}
