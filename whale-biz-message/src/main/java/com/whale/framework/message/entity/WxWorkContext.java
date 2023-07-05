package com.whale.framework.message.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.whale.framework.message.enums.WxWorkMsgTypeEnum.*;

/**
 * @Description: 企业微信通知消息请求体
 * @Author Whale
 * @Date: 2023/05/25 11:17 AM
 */
@Data
public class WxWorkContext {

    /**
     * 消息类型
     */
    private String msgtype;

    /**
     * 文本消息
     */
    private WeWorkText text;

    /**
     * markdown消息
     */
    private WeWorkMarkDown markdown;

    /**
     * 图片消息
     */
    private WeWorkImage image;

    /**
     * 图文消息
     */
    private WeWorkNew news;

    public WxWorkContext() {
    }

    public WxWorkContext(WeWorkText text) {
        this.text = text;
        this.msgtype = TEXT.getMsgType();
    }

    public WxWorkContext(WeWorkMarkDown markdown) {
        this.markdown = markdown;
        this.msgtype = MARKDOWN.getMsgType();
    }

    public WxWorkContext(WeWorkImage image) {
        this.image = image;
        this.msgtype = IMAGE.getMsgType();
    }

    public WxWorkContext(WeWorkNew news) {
        this.news = news;
        this.msgtype = NEWS.getMsgType();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkText {

        private String content;

        private String[] mentioned_list;

        private String[] mentioned_mobile_list;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkMarkDown {

        private String content;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkImage {

        private String media_id;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkNew {

        private List<WeWorkArticle> articles;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkArticle {

        private String title;

        private String description;

        private String url;

        private String picurl;

    }

}
