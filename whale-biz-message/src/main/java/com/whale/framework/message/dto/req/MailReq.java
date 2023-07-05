package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description: EmailReq
 * @Date: 2022/12/9 11:56 AM
 * @author Whale
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailReq extends BaseRequest {


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
    private List<AttachmentReq> attachments;

    /**
     * 邮件正文
     */
    private String context;

    /**
     * 操作用户id
     */
//    @NotBlank
    private String currentUserId;

    /**
     * 操作用户名称
     */
//    @NotBlank
    private String currentUserName;
}
