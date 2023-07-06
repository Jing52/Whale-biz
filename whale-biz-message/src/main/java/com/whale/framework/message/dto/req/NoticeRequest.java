package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description: NoticeReq
 * @Date: 2022/12/9 11:56 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class NoticeRequest extends BaseRequest {

    /**
     * 类型
     */
    @NotBlank
    @Schema(description = "类型")
    private String type;

    /**
     * 微信
     */
    @Schema(description = "微信")
    private WxWorkRequest wxWork;

    /**
     * 钉钉
     */
    @Schema(description = "钉钉")
    private DingTalkRequest dingTalkRequest;

    /**
     * 邮件
     */
    @Schema(description = "邮件")
    private MailRequest mailRequest;
}
