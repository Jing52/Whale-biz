package com.whale.framework.message.service;

import com.whale.framework.message.dto.req.DingTalkRequest;
import com.whale.framework.message.dto.req.MailRequest;
import com.whale.framework.message.dto.req.WxWorkRequest;

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
    String wxWorkNotice(WxWorkRequest wxWork);

    /**
     * 钉钉通知
     *
     * @param email
     */
    String dingTalkNotice(DingTalkRequest email);

    /**
     * 邮件通知
     *
     * @param email
     */
    Boolean mailNotice(MailRequest email);
}
