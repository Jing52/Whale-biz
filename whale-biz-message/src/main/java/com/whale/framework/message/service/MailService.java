package com.whale.framework.message.service;

import com.whale.framework.message.dto.req.MailReq;

/**
 * @Description: 邮件发送
 * @Author Whale
 * @Date: 2023/3/30 10:19 AM
 */
public interface MailService {

    /**
     * 有附件的邮件发送
     *
     * @param email
     */
    Boolean sendMail(MailReq email);

    /**
     * 无附件的邮件发送
     * @param email
     */
    Boolean sendMailWithoutAttachments(MailReq email);
}
