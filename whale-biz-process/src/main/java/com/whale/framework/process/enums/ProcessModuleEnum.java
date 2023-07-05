package com.whale.framework.process.enums;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 分页列表的策略枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessModuleEnum {

    TAB("TAB", "流程中心tab"),
    TODO("TODO", "待我处理"),
    DONE("DONE", "我已处理"),
    CC("CC", "抄送我的"),
    SENT("SENT", "已发事项"),
    READY("READY", "待发事项"),

    ;

    private String code;

    private String desc;

    ProcessModuleEnum(String code, String desc) {
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
