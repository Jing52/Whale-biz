package com.whale.framework.message.processor;


import com.whale.framework.message.entity.ExceptionContext;

/**
 * @Description: 异常信息通知处理接口
 * @Author Whale
 * @Date: 2023/3/30 10:37 AM
 */
@FunctionalInterface
public interface INoticeProcessor {

    /**
     * 异常信息通知
     *
     * @param exceptionInfo 异常信息
     */
    void sendNotice(ExceptionContext exceptionInfo);

}
