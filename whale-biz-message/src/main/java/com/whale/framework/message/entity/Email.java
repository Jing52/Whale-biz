package com.whale.framework.message.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description: 邮件
 * @Author Whale
 * @Date: 2023/3/22 10:13 AM
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class Email {

    /**
     * 邮件主题
     */
    @NotBlank
    private String subject;

    /**
     * 发送邮箱
     */
    private String from;

    /**
     * 接收邮箱
     */
    @NotNull
    private String[] to;

    /**
     * 接收邮箱
     */
    private String[] cc;

    /**
     * 收件人名称
     */
    private String toName;

    /**
     * 邮件附件
     */
    private List<Attachment> attachments;

    /**
     * 邮件正文
     */
    private String context;

    /**
     * 操作用户id
     */
    @NotBlank
    private String currentUserId;

    /**
     * 操作用户名称
     */
    @NotBlank
    private String currentUserName;
}
