package com.whale.framework.message.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 钉钉异常通知消息请求体
 * @Author Whale
 * @Date: 2023/3/22 10:30 AM
 */
@Data
public class DingTalkContext {

    /**
     * 消息类型
     */
    private String msgtype;

    /**
     * 文本消息
     */
    private DingTalkText text;

    /**
     * markdown消息
     */
    private DingTalkMarkDown markdown;

    /**
     * 跳转消息
     */
    private DingTalkActionCard actionCard;

    /**
     * 卡片消息
     */
    private DingTalkFeedCard feedCard;

    /**
     * @信息
     */
    private DingTalkAt at;

    public DingTalkContext(String msgtype, DingTalkText text, DingTalkAt at) {
        this.msgtype = msgtype;
        this.text = text;
        this.at = at;
    }

    public DingTalkContext(String msgtype, DingTalkMarkDown markdown, DingTalkAt at) {
        this.msgtype = msgtype;
        this.markdown = markdown;
        this.at = at;
    }

    public DingTalkContext(String msgtype, DingTalkActionCard actionCard, DingTalkAt at) {
        this.msgtype = msgtype;
        this.actionCard = actionCard;
        this.at = at;
    }

    public DingTalkContext(String msgtype, DingTalkFeedCard feedCard, DingTalkAt at) {
        this.msgtype = msgtype;
        this.feedCard = feedCard;
        this.at = at;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkText {

        private String content;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkMarkDown {

        private String title;

        private String text;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkActionCard {

        private String title;

        private String text;

        private String hideAvatar;

        private String btnOrientation;

        private List<DingTalkActionCardBtn> btns;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkActionCardBtn {

        private String title;

        private String actionURL;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkFeedCard {

        private List<DingTalkFeedCardLink> links;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkFeedCardLink {

        private String title;

        private String messageURL;

        private String picURL;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkAt {

        private String[] atMobiles;

        private boolean isAtAll;

    }
}
