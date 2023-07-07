package com.whale.framework.message.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * 因为正常拦截是 RestController和Controller注解下面的方法，那如果是一些异步操作就无法捕捉。
 * 这里针对一些异步方法无法捕获到异常的情况新增此注解
 * 只需要标记在异步方法的入口类上即可
 * @Author Whale
 * @Date: 2023/05/25 11:17 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ExceptionNotice {


}
