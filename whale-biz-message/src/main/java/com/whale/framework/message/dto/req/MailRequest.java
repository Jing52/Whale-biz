package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema
public class MailRequest extends BaseRequest {


    /**
     * 邮件主题
     */
    @Schema(description = "邮件主题")
    @NotBlank
    private String subject;

    /**
     * 发送邮箱
     */
    @Schema(description = "发送邮箱")
    private String from;

    /**
     * 接收邮箱
     */
    @Schema(description = "接收邮箱")
    @NotNull
    private String[] to;

    /**
     * 接收邮箱
     */
    @Schema(description = "接收邮箱")
    private String[] cc;

    /**
     * 收件人名称
     */
    @Schema(description = "收件人名称")
    private String toName;

    /**
     * 邮件附件
     */
    @Schema(description = "邮件附件")
    private List<AttachmentRequest> attachments;

    /**
     * 邮件正文
     */
    @Schema(description = "邮件正文")
    private String context;

    /**
     * 操作用户id
     */
//    @NotBlank
    @Schema(description = "操作用户id")
    private String currentUserId;

    /**
     * 操作用户名称
     */
//    @NotBlank
    @Schema(description = "操作用户名称")
    private String currentUserName;
}
