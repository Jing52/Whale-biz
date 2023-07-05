package com.whale.framework.process.annotation;

import com.whale.framework.process.enums.ProcessActionEnum;

import java.lang.annotation.*;

/**
 * @program: magus-engine
 * @description: 设置初始化端点
 * @packagename: com.magus.framework.auth.ascept
 * @author: Mr.Jing
 * @date: 2022-11-04 09:45:17
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcessActivity {

    ProcessActionEnum action() default ProcessActionEnum.ACTION_SUBMIT;
}
