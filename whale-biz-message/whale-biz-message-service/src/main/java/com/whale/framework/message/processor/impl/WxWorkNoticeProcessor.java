package com.whale.framework.message.processor.impl;


import com.whale.framework.message.entity.ExceptionContext;
import com.whale.framework.message.entity.WxWorkContext;
import com.whale.framework.message.processor.INoticeProcessor;
import com.whale.framework.message.properties.WxWorkProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: 企业微信异常信息通知具体实现
 * @Author Whale
 * @Date: 2023/05/25 11:17 AM
 */
public class WxWorkNoticeProcessor implements INoticeProcessor {

    private final WxWorkProperties properties;

    private final RestTemplate restTemplate;

    private final Log logger = LogFactory.getLog(getClass());

    public WxWorkNoticeProcessor(RestTemplate restTemplate,
                                 WxWorkProperties properties) {
        Assert.hasText(properties.getWebHook(), "WeWork's webHook must not be null");
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendNotice(ExceptionContext exception) {
        WxWorkContext.WeWorkMarkDown markdown = new WxWorkContext.WeWorkMarkDown(exception.createWeChatMarkDown());
        WxWorkContext context = new WxWorkContext(markdown);
        String result = restTemplate.postForObject(properties.getWebHook(), context, String.class);
        logger.info("企业微信机器人发送异常文本消息结果："+result);
    }

}
