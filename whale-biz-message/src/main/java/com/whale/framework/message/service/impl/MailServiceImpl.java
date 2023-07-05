package com.whale.framework.message.service.impl;

import com.whale.framework.message.annotation.ExceptionNotice;
import com.whale.framework.message.dto.req.MailReq;
import com.whale.framework.message.service.MailService;
import com.whale.framework.message.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Description: 邮件发送
 * @Author Whale
 * @Date: 2023/3/22 10:12 AM
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sysEmail;

    /**
     * 有附件的邮件发送
     *
     * @param email
     */
    @Override
    @Async
    @ExceptionNotice
    public Boolean sendMail(MailReq email) {
        String from = StringUtils.isBlank(email.getFrom()) ? sysEmail : email.getFrom();
        log.info("【{}】开始向邮箱【{}】发送邮件", from, email.getTo());
        if (StringUtils.isBlank(from)) {
            log.error("邮件无发送人");
            return null;
        }
        Boolean result = EmailUtil.sendMail(javaMailSender, StringUtils.isBlank(email.getFrom()) ? sysEmail : email.getFrom(),
                email.getTo(), email.getCc(), email.getSubject(), email.getContext(), email.getAttachments());
        log.info("【{}】开始向邮箱【{}】邮件发送成功", from, email.getTo());
        return result;
    }

    @Override
    @Async
    @ExceptionNotice
    public Boolean sendMailWithoutAttachments(MailReq email) {
        String from = StringUtils.isBlank(email.getFrom()) ? sysEmail : email.getFrom();
        log.info("【{}】开始向邮箱【{}】发送邮件", from, email.getTo());
        if (StringUtils.isBlank(from)) {
            log.error("邮件无发送人");
            return null;
        }
        Boolean result = EmailUtil.sendMailWithoutAttachments(javaMailSender, from, email.getTo(), email.getCc(), email.getSubject(), email.getContext());
        log.info("【{}】开始向邮箱【{}】邮件发送成功", from, email.getTo());
        return result;
    }
}

