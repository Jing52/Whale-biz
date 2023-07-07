package com.whale.framework.message.controller;

import com.whale.framework.message.client.domain.request.MailRequest;
import com.whale.framework.message.service.impl.MailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "邮件相关")
@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailServiceImpl sendMailService;

    @Operation(summary = "发送邮件")
    @PostMapping("/send")
    public void sendMail(@RequestBody MailRequest mail) {
        sendMailService.sendMail(mail);
    }

    @Operation(summary = "发送邮件（无附件）")
    @PostMapping("/send/without-attachments")
    public void sendMailWithoutAttachments(@RequestBody MailRequest mail) {
        sendMailService.sendMailWithoutAttachments(mail);
    }
}

