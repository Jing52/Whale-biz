package com.whale.framework.common.domain.response;

public enum ResponseCodeEnum {


    SUCCESS("000", "成功"),

    // 001 ～ 099 基本校验异常
    ILLEGAL_ARGUMENT("001", "参数错误: %s"),
    ILLEGAL_CONVERT("002", "参数转换错误"),
    ILLEGAL_REQUEST("003", "未知请求"),
    INVALID_PARAMS_LENGTH("004", "参数长度错误: %s"),
    INVALID_PARAMETER_VALUE("005", "参数值非法: %s"),
    PARAMS_IS_EMPTY("006", "必传参数不能为空: %s"),
    REQUEST_TIMEOUT("007", "请求超时"),
    AUTH_ACCESS_DENIED("008", "拒绝访问"),
    MISS_REQUIRED_PARAMETER("009", "缺失必选参数: %s"),
    DATA_NOT_EXISTS("010", "数据不存在"),
    SYSTEM_IS_BUSY("011", "系统繁忙"),
    DUPLICATE_REQUEST("012", "重复的请求"),
    BIZ_AUTHORITY_EXCEPTION("013", "当前用户没有功能权限！"),
    BIZ_DATA_AUTHORITY_EXCEPTION("014", "当前用户没有数据权限！"),
    DATA_EXPIRE_EXCEPTION("015", "数据已过期，请刷新重试！"),

    // 100 ～ 199 业务自定义异常
    BIZ_COMMON_EXCEPTION("100", "业务异常: %s"),
    BIZ_BUSINESS_EXCEPTION("101", "业务处理校验异常"),


    // 600 ～ 699 数据库异常
    REPOSITORY_COMMON_EXCEPTION("300", "数据仓储层统一异常: %s"),

    FAILURE("500", "请求服务发生内部错误，请联系相关同学解决！"),

    // 900 ～ 999 服务层面异常
    RATE_LIMITED("900", "服务过载，当前请求已被限流"),
    DEGRADATION("901", "服务已降级"),
    ;

    private final String code;
    private final String msg;

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getFormatMsg(String... params) {
        return String.format(this.getMsg(), params);
    }

    @Override
    public String toString() {
        return "ResponseCode(code=" + this.getCode() + ", msg=" + this.getMsg() + ")";
    }

    ResponseCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
