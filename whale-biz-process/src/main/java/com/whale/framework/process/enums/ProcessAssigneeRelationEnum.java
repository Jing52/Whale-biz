package com.whale.framework.process.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.enums
 * @Description: 操作人关系网枚举
 * @Author: Whale
 * @Date: 2022/12/6 1:41 PM
 */
public enum ProcessAssigneeRelationEnum {

    APPLICANT("applicant", "申请人", -1),
    APPLICANT_ORG("applicant_org", "申请人组织", 0),
    APPLICANT_TOP_ONE_ORG("applicant_top_one_org", "申请人上级组织", 1),
    APPLICANT_TOP_TWO_ORG("applicant_top_two_org", "申请人上上级组织", 2),
    APPLICANT_TOP_MAX_ORG("applicant_top_max_org", "申请人顶级组织", Integer.MAX_VALUE),

    ;

    private String code;

    private String desc;

    private int relation;

    ProcessAssigneeRelationEnum(String code, String desc, int relation) {
        this.code = code;
        this.desc = desc;
        this.relation = relation;
    }

    public static ProcessAssigneeRelationEnum findByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ProcessAssigneeRelationEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
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

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }
    
    public static Boolean applicantOrg(ProcessAssigneeRelationEnum relation) {
        if (ProcessAssigneeRelationEnum.APPLICANT_ORG == relation
                || ProcessAssigneeRelationEnum.APPLICANT_TOP_ONE_ORG == relation
                || ProcessAssigneeRelationEnum.APPLICANT_TOP_TWO_ORG == relation
                || ProcessAssigneeRelationEnum.APPLICANT_TOP_MAX_ORG == relation) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
