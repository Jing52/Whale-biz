package com.whale.framework.process.enums;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 流程状态的枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessStatusEnum {
    UNCOMMITTED("UNCOMMITTED", "未提交"),
    PENDING("PENDING", "处理中"),
    COMPLETED("COMPLETED", "完成"),

    REJECT("REJECT", "被驳回"),

    ;

    private String code;

    private String desc;

    ProcessStatusEnum(String code, String desc) {
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
