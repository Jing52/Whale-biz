package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/23 2:38 PM
 */
public enum ProcessScheduleHandleRuleEnum {

    AUTO_REMIND("AUTO_REMIND", "自动提醒"),
    AUTO_SUBMIT("AUTO_SUBMIT", "自动提交"),
    AUTO_REVERSE("AUTO_REVERSE", "自动撤回"),

    ;

    private String code;

    private String desc;

    ProcessScheduleHandleRuleEnum(String code, String desc) {
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
