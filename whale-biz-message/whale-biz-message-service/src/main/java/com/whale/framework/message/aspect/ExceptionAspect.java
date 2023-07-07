package com.whale.framework.message.aspect;

import com.whale.framework.message.handler.ExceptionNoticeHandler;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

/**
 * @Description: 异常拦截器
 * @Author Whale
 * @Date: 2023/05/25 11:17 AM
 */
@Aspect
@RequiredArgsConstructor
public class ExceptionAspect {

    private final ExceptionNoticeHandler handler;

    @AfterThrowing(value = "@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller) || @within(com.whale.framework.message.annotation.ExceptionNotice)",
            throwing = "e")
    public void doAfterThrow(JoinPoint joinPoint, Exception e) {
        handler.createNotice(e, joinPoint);
    }
}
