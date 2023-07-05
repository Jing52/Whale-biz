package com.whale.framework.process.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 操作行为的枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessActionEnum {

    ACTION_SUBMIT("ACTION_SUBMIT", "提交申请"),
    ACTION_DELEGATE("ACTION_DELEGATE", "委托"),
    ACTION_COMPLETE("ACTION_COMPLETE", "同意"),
    ACTION_CLAIM("ACTION_CLAIM", "认领"),
    ACTION_CARBON_COPY("ACTION_CARBON_COPY", "传阅"),
    ACTION_ASSIGNEE("ACTION_ASSIGNEE","分配任务"),
    ACTION_JUMP("ACTION_JUMP","跳转"),
    ACTION_REJECT_FIRST("ACTION_REJECT_FIRST","驳回"),
    ACTION_REVERSE("ACTION_REVERSE","撤回"),
    ACTION_BATCH_APPROVAL("ACTION_BATCH_APPROVAL","批量审批"),

    ;

    private String code;

    private String desc;

    ProcessActionEnum(String code, String desc) {
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

    public static List<ProcessActionEnum> getSystemAction() {
        return Arrays.asList(ACTION_COMPLETE, ACTION_REJECT_FIRST, ACTION_REVERSE, ACTION_JUMP, ACTION_CARBON_COPY, ACTION_DELEGATE, ACTION_BATCH_APPROVAL);
    }
}
