package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/4 10:13 AM
 */
public enum ProcessCompleteRuleEnum {

    CLOSE("CLOSE", "不启用"),
    START_USER("START_USER", "审批人为发起人"),
    NEIGHBOR("NEIGHBOR", "审批人与上一节点处理人相同"),
    PARTICIPATION("PARTICIPATION", "审批人处理过该流程"),

    ;

    private String code;

    private String desc;

    ProcessCompleteRuleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
