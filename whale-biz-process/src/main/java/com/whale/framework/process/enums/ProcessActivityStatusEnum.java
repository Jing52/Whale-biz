package com.whale.framework.process.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 活动动态的枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessActivityStatusEnum {

    SUBMIT("SUBMIT", "提交申请"),
    AGREE("AGREE", "同意"),
    REJECT("REJECT", "驳回"),
    DELEGATE("DELEGATE", "委托"),
    PENDING("PENDING", "处理中"),
    JUMP("JUMP", "跳转"),
    COMPLETED("COMPLETED", "已完成"),
    ;

    private String code;

    private String desc;

    ProcessActivityStatusEnum(String code, String desc) {
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

    public static String findDesc(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ProcessActivityStatusEnum value : ProcessActivityStatusEnum.values()) {
            if (code.equals(value.name())) {
                return value.desc;
            }
        }
        return null;
    }
}
