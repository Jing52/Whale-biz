package com.whale.framework.message.utils;

import com.whale.framework.message.dto.req.AttachmentReq;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 电子邮件工具类
 * @Author Whale
 * @Date: 2023/3/22 10:26 AM
 */
@Slf4j
public class EmailUtil {

    /**
     * 发送带有单个附件的邮件
     *
     * @param mailSender
     *            邮件工具实例 （客户端通过 @Autowired JavaMailSender javaMailSender 注入即可使用）
     *
     * @param fromEmail
     *            发件人邮箱地址（格式：abc@xxx.com）
     * @param toEmail
     *            收件人邮箱地址（格式：abc@xxx.com）
     * @param ccEmail
     *            抄送人邮箱地址（格式：abc@xxx.com）
     * @param subject
     *            主题
     * @param content
     *            正文
     * @return 0:失败，1：成功
     *
     * @author whale on 2023-03-17 22:25
     */
    public static Boolean sendMailWithoutAttachments(JavaMailSender mailSender, String fromEmail, String[] toEmail, String[] ccEmail, String subject, String content) {
        Boolean result = Boolean.TRUE;
        // 附件名完整显示
        System.setProperty("mail.mime.splitlongparameters", "false");
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setCc(ccEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("邮件发送成功,收件人地址:" + toEmail);
            return result;
        } catch (Exception e) {
            log.error("邮件发送出现异常,收件人地址:" + toEmail, e);
            result = Boolean.FALSE;
        }
        return result;
    }

    /**
     * 发送带有多个附件的邮件
     *
     * @param mailSender
     *            邮件工具实例 （客户端通过 @Autowired JavaMailSender javaMailSender 注入即可使用）
     *
     * @param fromEmail
     *            发件人邮箱地址（格式：abc@xxx.com）
     * @param toEmail
     *            收件人邮箱地址（格式：abc@xxx.com）
     * @param ccEmail
     *            抄送人邮箱地址（格式：abc@xxx.com）
     * @param subject
     *            主题
     * @param content
     *            正文
     * @param attachments
     *            多个附件列表
     * @return 0:失败，1：成功
     * @author whale on 2023-03-17 22:16
     */
    public static Boolean sendMail(JavaMailSender mailSender, String fromEmail, String[] toEmail, String[] ccEmail,
                                   String subject, String content, List<AttachmentReq> attachments) {
        Boolean result = Boolean.TRUE;
        System.setProperty("mail.mime.splitlongparameters", "false");
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setCc(ccEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            if (CollectionUtils.isNotEmpty(attachments)) {
                attachments.forEach((attachment) -> {
                    try {
                        // 加载文件资源，作为附件
                        FileSystemResource file = new FileSystemResource(attachment.getFilePath());
                        // 添加附件
                        helper.addAttachment(MimeUtility.encodeWord(attachment.getFileName(), "UTF-8", "B"), file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            mailSender.send(message);
            log.info("邮件发送成功,收件人地址:" + toEmail);
        } catch (Exception e) {
            log.error("邮件发送出现异常,收件人地址:" + toEmail, e);
            result = Boolean.FALSE;
        }
        return result;
    }

    private static Boolean checkMail(String[] mails) {
        for (String mail : mails) {
            Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
            Matcher matcher = pattern.matcher(mail);
            if (!matcher.matches()) {
                log.error("邮箱格式不合法");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
}


