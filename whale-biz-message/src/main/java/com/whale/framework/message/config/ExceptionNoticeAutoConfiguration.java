package com.whale.framework.message.config;

import com.whale.framework.message.aspect.ExceptionAspect;
import com.whale.framework.message.handler.ExceptionNoticeHandler;
import com.whale.framework.message.processor.impl.DingTalkNoticeProcessor;
import com.whale.framework.message.processor.INoticeProcessor;
import com.whale.framework.message.processor.impl.MailNoticeProcessor;
import com.whale.framework.message.processor.impl.WxWorkNoticeProcessor;
import com.whale.framework.message.properties.DingTalkProperties;
import com.whale.framework.message.properties.ExceptionNoticeProperties;
import com.whale.framework.message.properties.MailProperties;
import com.whale.framework.message.properties.WxWorkProperties;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 异常消息通知配置
 * @Author Whale
 * @Date: 2023/05/24 11:17 AM
 */
@Configuration
@ConditionalOnProperty(prefix = ExceptionNoticeProperties.PREFIX, name = "enable", havingValue = "true")
@EnableConfigurationProperties(value = ExceptionNoticeProperties.class)
public class ExceptionNoticeAutoConfiguration {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * 注入 异常处理bean
     *
     * @param properties
     * @return
     */
    @Bean(initMethod = "start")
    public ExceptionNoticeHandler noticeHandler(ExceptionNoticeProperties properties) {
        List<INoticeProcessor> noticeProcessors = new ArrayList<>(3);
        INoticeProcessor noticeProcessor;

        // 邮件
        MailProperties email = properties.getMail();
        if (BooleanUtils.isTrue(email.getEnable()) && null != mailSender) {
            noticeProcessor = new MailNoticeProcessor(mailSender, email);
            noticeProcessors.add(noticeProcessor);
        }

        // 钉钉
        DingTalkProperties dingTalk = properties.getDingTalk();
        if (BooleanUtils.isTrue(dingTalk.getEnable())) {
            noticeProcessor = new DingTalkNoticeProcessor(restTemplate, dingTalk);
            noticeProcessors.add(noticeProcessor);
        }

        // 企业微信
        WxWorkProperties wxWork = properties.getWxWork();
        if (BooleanUtils.isTrue(wxWork.getEnable())) {
            noticeProcessor = new WxWorkNoticeProcessor(restTemplate, wxWork);
            noticeProcessors.add(noticeProcessor);
        }

        return new ExceptionNoticeHandler(properties, noticeProcessors);
    }

    /**
     * 注入异常捕获aop
     *
     * @param noticeHandler
     * @return
     */
    @Bean
    @ConditionalOnClass(ExceptionNoticeHandler.class)
    public ExceptionAspect exceptionListener(ExceptionNoticeHandler noticeHandler) {
        return new ExceptionAspect(noticeHandler);
    }
}

