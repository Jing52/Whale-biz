package com.whale.framework.message.controller;

import com.whale.framework.message.dto.req.MailReq;
import com.whale.framework.message.service.impl.MailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 邮件
 * @Author Whale
 * @Date: 2023/3/22 10:12 AM
 */
@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailServiceImpl sendMailService;

    @PostMapping("/send")
    public void sendMail(@RequestBody MailReq mail) {
        sendMailService.sendMail(mail);
    }

    @PostMapping("/send/without-attachments")
    public void sendMailWithoutAttachments(@RequestBody MailReq mail) {
        sendMailService.sendMailWithoutAttachments(mail);
    }
}

