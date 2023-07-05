package com.whale.framework.process.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 设置操作人选项的枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessAssigneeTypeEnum {
    $APPLICANT("APPLICANT", "APPLICANT_", "申请人"),
    $USER("USER", "USER_", "用户"),
    $ORG("ORG", "ORG_", "组织"),
    $WORKGROUP("WORKGROUP", "GROUP_", "工作组"),
    $DUTY("DUTY", "DUTY_", "职务"),
    $RELATION("RELATION", "RELATION_", "关系"),

    ;

    private String code;

    private String pre;

    private String desc;

    ProcessAssigneeTypeEnum(String code, String pre, String desc) {
        this.code = code;
        this.pre = pre;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ProcessAssigneeTypeEnum findAssigneeByCode(String code) {
        for (ProcessAssigneeTypeEnum value : values()) {
            if (StringUtils.equals(value.code, code)) {
                return value;
            }
        }
        return null;
    }
}
