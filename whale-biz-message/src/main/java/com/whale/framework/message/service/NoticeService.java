package com.whale.framework.message.service;

import com.whale.framework.message.dto.req.DingTalkReq;
import com.whale.framework.message.dto.req.MailReq;
import com.whale.framework.message.dto.req.WxWorkReq;

/**
 * @Description: 通知
 * @Author Whale
 * @Date: 2023/3/30 10:19 AM
 */
public interface NoticeService {

    /**
     * 企业微信通知
     *
     * @param wxWork
     */
    String wxWorkNotice(WxWorkReq wxWork);

    /**
     * 钉钉通知
     *
     * @param email
     */
    String dingTalkNotice(DingTalkReq email);

    /**
     * 邮件通知
     *
     * @param email
     */
    Boolean mailNotice(MailReq email);
}
