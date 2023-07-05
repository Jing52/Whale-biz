package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/4 10:13 AM
 */
public enum ProcessRejectRuleEnum {

    FROM_START_NODE("FROM_START_NODE", "从头开始审批"),
    FROM_REJECT_NODE("FROM_REJECT_NODE", "从驳回节点审批"),

    ;

    private String code;

    private String desc;

    ProcessRejectRuleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
