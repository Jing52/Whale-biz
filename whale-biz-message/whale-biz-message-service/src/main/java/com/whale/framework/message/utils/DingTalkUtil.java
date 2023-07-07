package com.whale.framework.message.utils;

import com.whale.framework.message.enums.DingTalkMsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 钉钉工具类
 * @Author Whale
 * @Date: 2023/5/24 5:39 PM
 */
@Slf4j
public class DingTalkUtil {

    private final static RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送文本消息
     *
     * 纯文本类型是最基本的文本类型，可以发送纯文本消息到钉钉机器人。纯文本消息无格式，只包含一段简单的文本内容。
     *
     * @param webHook       webHook地址
     * @param text          文本
     *                      示例：{
     *                               "text": {
     *                                   "content": "这是一条纯文本消息"
     *                               }
     *                           }
     * @param at                @信息
     *                          1. @具体用户:   key:atMobiles;value:List;
     *                          2. @所有人:    key:isAtAll;value:true;
     *                          示例：{
     *                                  "at": {
     *                                      "atMobiles": ["153xxxx8888", "139xxxx1234"],
     *                                      "isAtAll": true
     *                                  }
     *                               }
     * @return
     */
    public static String sendTextMsg(String webHook, Map<String,Object> text, Map<String,Object> at) {
        Assert.hasText(webHook, "DingTalk's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", DingTalkMsgTypeEnum.TEXT);
        input.put("text", text);
        input.put("at", at);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("钉钉机器人发送文本消息结果：{}", result);
        return result;
    }

    /**
     * 发送markdown文本消息
     *
     * Markdown是一种标记语言，可以将文本内容转换成具有格式的内容，并且在钉钉机器人中也支持Markdown语法。使用Markdown可以实现文字加粗、断行、列表、图片等效果，实现更丰富的信息展示。
     *
     * @param webHook       webHook地址
     * @param markdown          文本
     *                          示例：{
     *                                   "markdown": {
     *                                       "title": "这是一条Markdown消息",
     *                                       "text": "**Hello, world!**"
     *                                   }
     *                               }
     * @param at                @信息
     *                          1. @具体用户:   key:atMobiles;value:List;
     *                          2. @所有人:    key:isAtAll;value:true;
     *                          示例：{
     *                                  "at": {
     *                                      "atMobiles": ["153xxxx8888", "139xxxx1234"],
     *                                      "isAtAll": true
     *                                  }
     *                               }
     * @return
     */
    public static String sendMarkDownMsg(String webHook, Map<String,Object> markdown, Map<String,Object> at) {
        Assert.hasText(webHook, "DingTalk's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", DingTalkMsgTypeEnum.MARKDOWN);
        input.put("markdown", markdown);
        input.put("at", at);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("钉钉机器人发送markdown文本消息结果：{}", result);
        return result;
    }

    /**
     * 发送ActionCard类型消息
     *
     * ActionCard是一种具有交互性的文本类型，可以通过设置按钮等元素展示交互场景，例如单选、多选、打开应用等。
     *
     * @param webHook           webHook地址
     * @param actionCard        内容
     *                          示例：{
     *                                  "actionCard": {
     *                                      "title": "这是一条ActionCard消息",
     *                                      "text": "请回答以下问题：",
     *                                      "hideAvatar": "0",
     *                                      "btnOrientation": "0",
     *                                      "btns": [
     *                                          {
     *                                              "title": "百度",
     *                                              "actionURL": "http://www.baidu.com"
     *                                          },
     *                                          {
     *                                              "title": "谷歌",
     *                                              "actionURL": "http://www.google.com"
     *                                          }
     *                                      ]
     *                                  }
     *                              }
     * @param at                @信息
     *                          1. @具体用户:   key:atMobiles;value:List;
     *                          2. @所有人:    key:isAtAll;value:true;
     *                          示例：{
     *                                  "at": {
     *                                      "atMobiles": ["153xxxx8888", "139xxxx1234"],
     *                                      "isAtAll": true
     *                                  }
     *                               }
     * @return
     */
    public static String sendActionCardMsg(String webHook, Map<String, Object> actionCard, Map<String,Object> at) {
        Assert.hasText(webHook, "DingTalk's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", DingTalkMsgTypeEnum.ACTION_CARD);
        input.put("actionCard", actionCard);
        input.put("at", at);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("钉钉机器人发送交互性文本消息结果：{}", result);
        return result;
    }

    /**
     * 发送FeedCard类型消息
     *
     * FeedCard是一种图文消息类型，一般用于展示新闻等具有图文结构的信息。FeedCard支持最多展示10个图文信息，并且可以设置点击跳转链接。
     *
     * @param webHook           webHook地址
     * @param feedCard          图文信息（数量<=10个）
     *                          示例: {
     *                                "msgtype": "feedCard",
     *                                "feedCard": {
     *                                    "links": [
     *                                        {
     *                                            "title": "百度一下，你就知道",
     *                                            "messageURL": "http://www.baidu.com",
     *                                            "picURL": "http://www.baidu.com/img/bd_logo1.png"
     *                                        },
     *                                        {
     *                                            "title": "Google搜索引擎",
     *                                            "messageURL": "http://www.google.com",
     *                                            "picURL": "http://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
     *                                        }
     *                                    ]
     *                                }
     *
     * @param at                @信息
     *                          1. @具体用户:   key:atMobiles;value:List;
     *                          2. @所有人:    key:isAtAll;value:true;
     *                          示例：{
     *                                  "at": {
     *                                      "atMobiles": ["153xxxx8888", "139xxxx1234"],
     *                                      "isAtAll": true
     *                                  }
     *                               }
     * @return
     */
    public static String sendFeedCardMsg(String webHook, Map<String, List<Map<String, Object>>> feedCard, Map<String,Object> at) {
        Assert.hasText(webHook, "DingTalk's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", DingTalkMsgTypeEnum.ACTION_CARD);
        input.put("feedCard", feedCard);
        input.put("at", at);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("钉钉机器人发送图文消息结果：{}", result);
        return result;
    }
}
