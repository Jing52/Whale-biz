package com.whale.framework.process.enums;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description:
 * @Date: 2023/5/23 2:38 PM
 */
public enum ProcessFieldPermissionEnum {

    VIEW("VIEW", "查看"),
    EDIT("EDIT", "编辑"),
    REQUIRED("REQUIRED", "必填"),

    ;

    private String code;

    private String desc;

    ProcessFieldPermissionEnum(String code, String desc) {
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
