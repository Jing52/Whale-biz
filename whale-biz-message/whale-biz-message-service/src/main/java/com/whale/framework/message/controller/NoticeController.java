package com.whale.framework.message.controller;

import com.whale.framework.common.domain.exception.BizServiceException;
import com.whale.framework.common.domain.response.BaseResponse;
import com.whale.framework.message.client.api.NoticeClient;
import com.whale.framework.message.client.domain.request.NoticeRequest;
import com.whale.framework.message.enums.NoticeTypeEnum;
import com.whale.framework.message.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.whale.framework.common.domain.response.ResponseCodeEnum.*;

/**
 * @Description: 异常通知
 * @Author Whale
 * @Date: 2023/05/24 11:17 AM
 */
@Tag(name = "通知相关")
@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController implements NoticeClient {

    @Autowired
    NoticeService noticeService;

    @Override
    @Operation(summary = "通知")
    @GetMapping()
    public BaseResponse notice(@RequestBody NoticeRequest noticeRequest) {
        try {
            String type = noticeRequest.getType();
            if (StringUtils.isBlank(type)) {
                return BaseResponse.success(null);
            }
            switch (NoticeTypeEnum.valueOf(type)) {
                case WX_WORK -> {
                    noticeService.wxWorkNotice(noticeRequest.getWxWork());
                }
                case DING_TALK -> {
                    noticeService.dingTalkNotice(noticeRequest.getDingTalkRequest());
                }
                case MAIL -> {
                    noticeService.mailNotice(noticeRequest.getMailRequest());
                }
                default -> {
                    throw new BizServiceException(ILLEGAL_ARGUMENT.getCode(), ILLEGAL_ARGUMENT.getFormatMsg("通知不支持该应用类型"));
                }
            }
        } catch (Exception e) {
            throw new BizServiceException(BIZ_COMMON_EXCEPTION.getCode(), BIZ_COMMON_EXCEPTION.getFormatMsg("通知失败"));
        }
        return BaseResponse.success(null);
    }
}
