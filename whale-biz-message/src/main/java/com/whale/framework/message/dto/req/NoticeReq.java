package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description: NoticeReq
 * @Date: 2022/12/9 11:56 AM
 */
@Data
public class NoticeReq extends BaseRequest {

    /**
     * 类型
     */
    @NotBlank
    private String type;

    /**
     * 微信
     */
    private WxWorkReq wxWork;

    /**
     * 钉钉
     */
    private DingTalkReq dingTalkReq;

    /**
     * 钉钉
     */
    private MailReq mailReq;
}
