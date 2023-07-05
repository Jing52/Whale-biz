package com.whale.framework.common.dto.enums;

/**
 * MapUtil 逻辑运算符号
 */
public enum TemplateDataLogicEnum {

    DATA_SPLIT("@","数据分割符"),
    JOIN_STR("JOIN_STR","拼接字符串"),
    SORT_NOT_NULL_MAPPING_VALUE("SORT_NOT_NULL_MAPPING_VALUE","非空映射值"),
    DOT_MARK(".", "点号，用来映射分隔.属性"),
    POUND_SIGN("#", "井号，用来判断"),
    DATE_FORMATTING("DATE_FORMAT_", "时间分隔符"),
    RADIO_SYMBOL("RADIO_SYMBOL", "选择标志位"),
    PERCENT_SYMBOL("%", "PERCENT_SYMBOL"),//用于文件显示%
    STR_SYMBOL("$", "用来表示字符串的处理"),
    MAX_SYMBOL("99999", "用来表示最大值"),
    METHOD("METHOD", "用来调用处理方法")
    ;

    private String type;
    private String cName;

    public String getType() {
        return type;
    }

    public String getcName() {
        return cName;
    }

    TemplateDataLogicEnum(String type, String cName) {
        this.type = type;
        this.cName = cName;
    }

}
