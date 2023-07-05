package com.whale.framework.message.processor.impl;


import com.whale.framework.message.entity.ExceptionContext;
import com.whale.framework.message.processor.INoticeProcessor;
import com.whale.framework.message.properties.MailProperties;
import com.whale.framework.message.utils.EmailUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.Assert;

/**
 * @Description: 邮箱异常信息通知具体实现
 * @Author Whale
 * @Date: 2023/05/25 11:17 AM
 */
public class MailNoticeProcessor implements INoticeProcessor {
    private final static String SUBJECT = "来自%s项目的异常通知";

    private final MailProperties mailProperties;

    private final JavaMailSender mailSender;

    public MailNoticeProcessor(JavaMailSender mailSender, MailProperties emailProperties) {
        Assert.noNullElements(emailProperties.getTo(), "email 'from' property must not be null");
        Assert.noNullElements(emailProperties.getTo(), "email 'to' property must not be null");
        this.mailSender = mailSender;
        this.mailProperties = emailProperties;
    }

    @Override
    public void sendNotice(ExceptionContext exceptionContext) {
        EmailUtil.sendMailWithoutAttachments(mailSender,
                mailProperties.getFrom(), mailProperties.getTo(), mailProperties.getCc(),
                String.format(SUBJECT, exceptionContext.getProject()), exceptionContext.createHtml());
    }

}
