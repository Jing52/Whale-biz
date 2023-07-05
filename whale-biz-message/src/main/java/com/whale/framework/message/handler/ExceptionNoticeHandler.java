package com.whale.framework.message.handler;

import com.whale.framework.message.entity.ExceptionContext;
import com.whale.framework.message.processor.INoticeProcessor;
import com.whale.framework.message.properties.ExceptionNoticeProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description: 异常通知处理
 * @Author Whale
 * @Date: 2023/3/30 10:37 AM
 */
@Slf4j
public class ExceptionNoticeHandler {

    private final String SEPARATOR = System.getProperty("line.separator");

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    private final BlockingQueue<ExceptionContext> exceptionInfoBlockingDeque = new ArrayBlockingQueue<>(1024);

    private final ExceptionNoticeProperties exceptionProperties;

    private final List<INoticeProcessor> noticeProcessors;

    public ExceptionNoticeHandler(ExceptionNoticeProperties exceptionProperties,
                                  List<INoticeProcessor> noticeProcessors) {
        this.exceptionProperties = exceptionProperties;
        this.noticeProcessors = noticeProcessors;
    }

    /**
     * 将捕获到的异常信息封装好之后发送到阻塞队列
     */
    public Boolean createNotice(Exception ex, JoinPoint joinPoint) {
        //校验当前异常是否是需要 排除的需要统计的异常
        if (containsException(ex)) {
            return null;
        }
        log.error("捕获到异常开始发送消息通知:{}method:{}--->", SEPARATOR, joinPoint.getSignature().getName());
        //获取请求参数
        Object parameter = getParameter(joinPoint);
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String address = null;
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //获取请求地址
            address = request.getRequestURL().toString() + ((request.getQueryString() != null && request.getQueryString().length() > 0) ? "?" + request.getQueryString() : "");
        }

        ExceptionContext exceptionContext = new ExceptionContext(ex, joinPoint.getSignature().getName(), exceptionProperties.getIncludedTracePackage(), parameter, address);
        exceptionContext.setProject(exceptionProperties.getProjectName());
        return exceptionInfoBlockingDeque.offer(exceptionContext);
    }

    /**
     * 启动定时任务发送异常通知
     */
    public void start() {
        executor.scheduleAtFixedRate(() -> {
            ExceptionContext exceptionContext = exceptionInfoBlockingDeque.poll();
            if (null != exceptionContext) {
                noticeProcessors.forEach(noticeProcessor -> noticeProcessor.sendNotice(exceptionContext));
            }
        }, 0, exceptionProperties.getPeriod(), TimeUnit.SECONDS);
    }

    /**
     * 排除的需要统计的异常
     */
    private boolean containsException(Exception exception) {
        Class<? extends Exception> exceptionClass = exception.getClass();
        List<Class<? extends Exception>> list = exceptionProperties.getExcludeExceptions();
        for (Class<? extends Exception> clazz : list) {
            //校验是否存在
            if (clazz.isAssignableFrom(exceptionClass)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 根据方法和传入的参数获取请求参数
     *
     * 注意这里就需要在参数前面加对应的RequestBody 和 RequestParam 注解
     */
    private Object getParameter(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();

        Object[] args = joinPoint.getArgs();
        List<Object> argList = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>(1);
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}
