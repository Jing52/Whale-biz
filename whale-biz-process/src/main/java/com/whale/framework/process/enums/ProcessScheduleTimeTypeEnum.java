package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/23 2:38 PM
 */
public enum ProcessScheduleTimeTypeEnum {

    CUSTOM("CUSTOM", "自定义事件"),
    FORM("FORM", "表单内日期时间字段"),

    ;

    private String code;

    private String desc;

    ProcessScheduleTimeTypeEnum(String code, String desc) {
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
