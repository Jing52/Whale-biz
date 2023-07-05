package com.whale.framework.message.utils;

import com.whale.framework.message.enums.WxWorkMsgTypeEnum;
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
 * @Description: 企业微信工具类
 * @Author Whale
 * @Date: 2023/5/24 5:39 PM
 */
@Slf4j
public class WxWorkUtil {

    private final static RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送文本消息
     *
     * @param webHook           webHook地址
     * @param text              文本
     *                          示例：{
     *                                   "text": {
     *                                       "content": "这是一条纯文本消息"
     *                                       "mentioned_list": ["user1", "user2"]
     *                                       "mentioned_mobile_list": ["139xxxx2312", "159xxxx1234"]
     *                                   }
     *                               }
     * @return
     */
    public static String sendTextMsg(String webHook, Map<String, Object> text) {
        Assert.hasText(webHook, "WeWork's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", WxWorkMsgTypeEnum.TEXT);
        input.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("企业微信机器人发送文本消息结果：{}", result);
        return result;
    }

    /**
     * 发送markdown文本消息
     *
     * 支持发送排版格式较好的文本消息，包括标准Markdown语法。
     *
     * @param webHook           webHook地址
     * @param markdown          文本
     *                          示例：{
     *                                   "markdown": {
     *                                       "content": "这是一条Markdown消息"
     *                                   }
     *                               }
     * @return
     */
    public static String sendMarkDownTextMsg(String webHook, Map<String, Object> markdown) {
        Assert.hasText(webHook, "WeWork's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", WxWorkMsgTypeEnum.MARKDOWN);
        input.put("markdown", markdown);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("企业微信机器人发送markdown文本消息结果：{}", result);
        return result;
    }

    /**
     * 发送图片消息
     *
     * 支持发送图片，JPG、PNG格式均可，图片大小不超过2MB。
     *
     * @param webHook           webHook地址
     * @param image             图片
     *                          示例：{
     *                                   "image": {
     *                                       "media_id": "MEDIA_ID"
     *                                   }
     *                               }
     * @return
     */
    public static String sendImageMsg(String webHook, Map<String, Object> image) {
        Assert.hasText(webHook, "WeWork's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", WxWorkMsgTypeEnum.IMAGE);
        input.put("image", image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("企业微信机器人发送图片消息结果：{}", result);
        return result;
    }

    /**
     * 发送图文消息
     *
     * 支持发送图文消息，可组合多条文本与图片消息。
     *
     * @param webHook           webHook地址
     * @param news              图文
     *                          示例：{
     *                                  "news": {
     *                                      "articles": [
     *                                          {
     *                                              "title": "图文消息标题1",
     *                                              "description": "图文消息描述1",
     *                                              "url": "http://www.example.com/article1.html",
     *                                              "picurl": "http://www.example.com/article1.jpg"
     *                                          },
     *                                          {
     *                                              "title": "图文消息标题2",
     *                                              "description": "图文消息描述2",
     *                                              "url": "http://www.example.com/article2.html",
     *                                              "picurl": "http://www.example.com/article2.jpg"
     *                                          }
     *                                      ]
     *                                  }
     *                              }
     * @return
     */
    public static String sendNewsMsg(String webHook, Map<String, List<Map<String, Object>>> news) {
        Assert.hasText(webHook, "WeWork's webHook must not be null");
        Map<String, Object> input = new HashMap<>();
        input.put("msgtype", WxWorkMsgTypeEnum.NEWS);
        input.put("news", news);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(input, headers);
        String result = restTemplate.postForObject(webHook, entity, String.class);
        log.info("企业微信机器人发送图文消息结果：{}", result);
        return result;
    }


}
