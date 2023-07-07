package com.whale.framework.message.processor.impl;


import com.whale.framework.message.entity.DingTalkContext;
import com.whale.framework.message.entity.ExceptionContext;
import com.whale.framework.message.enums.DingTalkMsgTypeEnum;
import com.whale.framework.message.processor.INoticeProcessor;
import com.whale.framework.message.properties.DingTalkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: 钉钉异常信息通知具体实现
 * @Author Whale
 * @Date: 2023/3/30 10:37 AM
 */
@Slf4j
public class DingTalkNoticeProcessor implements INoticeProcessor {

    private final DingTalkProperties dingTalkProperties;

    private final RestTemplate restTemplate;

    public DingTalkNoticeProcessor(RestTemplate restTemplate,
                                   DingTalkProperties dingTalkProperties) {
        Assert.hasText(dingTalkProperties.getWebHook(), "DingTalk webHook must not be null");
        this.dingTalkProperties = dingTalkProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendNotice(ExceptionContext exceptionContext) {
        DingTalkContext.DingTalkMarkDown markdown = new DingTalkContext.DingTalkMarkDown(exceptionContext.getProject() + "异常告警", exceptionContext.createDingTalkMarkDown());
        DingTalkContext.DingTalkAt at = new DingTalkContext.DingTalkAt(dingTalkProperties.getAtMobiles(), dingTalkProperties.getIsAtAll());
        DingTalkContext dingDingNotice = new DingTalkContext(DingTalkMsgTypeEnum.MARKDOWN.getMsgType(), markdown, at);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DingTalkContext> entity = new HttpEntity<>(dingDingNotice, headers);
        String result = restTemplate.postForObject(dingTalkProperties.getWebHook(), entity, String.class);
        log.debug(String.valueOf(result));
    }

}
