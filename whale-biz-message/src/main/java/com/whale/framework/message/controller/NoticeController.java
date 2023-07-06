package com.whale.framework.message.controller;

import com.whale.framework.message.dto.req.NoticeRequest;
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

/**
 * @Description: 异常通知
 * @Author Whale
 * @Date: 2023/05/24 11:17 AM
 */
@Tag(name = "通知相关")
@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    @Operation(summary = "通知")
    @GetMapping()
    public void notice(@RequestBody NoticeRequest noticeRequest) throws Exception {
        String type = noticeRequest.getType();
        if (StringUtils.isBlank(type)) {
            return;
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
                throw new Exception("通知不支持该应用类型");
            }
        }
    }
}
