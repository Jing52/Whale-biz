package com.whale.framework.process.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.generator.enums
 * @Description: 模板分类
 * @Date: 2023/4/24 3:53 PM
 */
@Getter
@AllArgsConstructor
public enum ProcessTemplateEnum {

    /**
     * 系统模板
     */
    SYSTEM("system", "系统模板"),
    /**
     * 公司模板
     */
    COMPANY("company", "公司模板"),
    /**
     * 我的模板
     */
    MINE("mine", "我的模板"),
    ;

    private final String code;
    private final String desc;
}
