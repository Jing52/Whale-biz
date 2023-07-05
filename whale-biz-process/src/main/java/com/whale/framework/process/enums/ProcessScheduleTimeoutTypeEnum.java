package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/23 2:38 PM
 */
public enum ProcessScheduleTimeoutTypeEnum {

    CURRENT_TIME("CURRENT_TIME", "在节点处理截止时间当时"),
    BEFORE_TIME("BEFORE_TIME", "在节点处理截止时间之前"),
    AFTER_TIME("AFTER_TIME", "在节点处理截止时间之后"),

    ;

    private String code;

    private String desc;

    ProcessScheduleTimeoutTypeEnum(String code, String desc) {
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
