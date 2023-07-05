package com.whale.framework.message.enums;

/**
 * @Description: 钉钉文本类型枚举
 * @Author Whale
 * @Date: 2023/3/30 10:37 AM
 */
public enum DingTalkMsgTypeEnum {

    TEXT("text"),
    MARKDOWN("markdown"),
    ACTION_CARD("action_card"),
    FEED_CARD("feed_card"),
    ;

    private final String msgType;

    public String getMsgType() {
        return msgType;
    }

    DingTalkMsgTypeEnum(String msgType) {
        this.msgType = msgType;
    }
}
