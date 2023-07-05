package com.whale.framework.message.controller;

import com.whale.framework.message.dto.req.NoticeReq;
import com.whale.framework.message.enums.NoticeTypeEnum;
import com.whale.framework.message.service.NoticeService;
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
@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    @GetMapping()
    public void notice(@RequestBody NoticeReq noticeReq) throws Exception {
        String type = noticeReq.getType();
        if (StringUtils.isBlank(type)) {
            return;
        }
        switch (NoticeTypeEnum.valueOf(type)) {
            case WX_WORK -> {
                noticeService.wxWorkNotice(noticeReq.getWxWork());
            }
            case DING_TALK -> {
                noticeService.dingTalkNotice(noticeReq.getDingTalkReq());
            }
            case MAIL -> {
                noticeService.mailNotice(noticeReq.getMailReq());
            }
            default -> {
                throw new Exception("通知不支持该应用类型");
            }
        }
    }
}
