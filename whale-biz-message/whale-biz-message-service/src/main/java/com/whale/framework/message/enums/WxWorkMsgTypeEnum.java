package com.whale.framework.message.enums;

/**
 *  企业微信文本类型枚举
 *
 * @author xub
 * @date 2022/3/29 下午4:44
 */
public enum WxWorkMsgTypeEnum {

    TEXT("text"),
    MARKDOWN("markdown"),
    IMAGE("image"),
    NEWS("news");

    private final String msgType;

    public String getMsgType() {
        return msgType;
    }

    WxWorkMsgTypeEnum(String msgType) {
        this.msgType = msgType;
    }
}
