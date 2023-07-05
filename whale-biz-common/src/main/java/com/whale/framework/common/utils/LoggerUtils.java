package com.whale.framework.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 日志
 * @Author Whale
 * @Date: 2023/6/27 10:12 AM
 */
public class LoggerUtils {


    public static final String ERROR_LOGGER = "error";

    public static Logger getLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }


    /**
     * Debug 输出
     *
     * @param logger  日志logger
     * @param message 输出信息
     */
    public static void debug(Logger logger, String message) {
        logger.debug(message);
    }

    /**
     * Debug 输出
     *
     * @param logger    日志logger
     * @param fmtString 输出信息key
     * @param value     输出信息value
     */
    public static void fmtDebug(Logger logger, String fmtString, Object... value) {
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if (null != value && value.length != 0) {
            fmtString = String.format(fmtString, value);
        }
        debug(logger, fmtString);
    }

    /**
     * Debug 输出
     *
     * @param logger  日志logger
     * @param message 输出信息
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }

    /**
     * Debug 输出
     *
     * @param logger  日志logger
     * @param message 输出信息
     */
    public static void warn(Logger logger, String message) {
        logger.warn(message);
    }

    /**
     * Info 输出
     *
     * @param logger    日志logger
     * @param fmtString 输出信息key
     * @param value     输出信息value
     */
    public static void fmtInfo(Logger logger, String fmtString, Object... value) {
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if (null != value && value.length != 0) {
            fmtString = String.format(fmtString, value);
        }
        info(logger, fmtString);
    }

    /**
     * Error 输出
     *
     * @param logger  日志logger
     * @param message 输出信息
     * @param e       异常类
     */
    public static void error(Logger logger, String message, Exception e) {
        if (null == e) {
            error(logger, message);
            return;
        } else {
            logger.error(message, e);
            getLogger(ERROR_LOGGER).error(message, e);
        }
    }

    /**
     * Error 输出
     *
     * @param logger  日志logger
     * @param message 输出信息
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
        getLogger(ERROR_LOGGER).error(message);
    }

    /**
     * warn 输出
     *
     * @param logger    日志logger
     * @param fmtString 输出信息key
     * @param value     输出信息value
     */
    public static void fmtWarn(Logger logger, String fmtString, Object... value) {
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if (null != value && value.length != 0) {
            fmtString = String.format(fmtString, value);
        }
        warn(logger, fmtString);
    }

    /**
     * 异常填充值输出
     *
     * @param logger    日志logger
     * @param fmtString 输出信息key
     * @param e         异常类
     * @param value     输出信息value
     */
    public static void fmtError(Logger logger, Exception e, String fmtString, Object... value) {
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if (null != value && value.length != 0) {
            fmtString = String.format(fmtString, value);
        }
        error(logger, fmtString, e);
    }

    /**
     * 异常填充值输出
     *
     * @param logger    日志logger
     * @param fmtString 输出信息key
     * @param value     输出信息value
     */
    public static void fmtError(Logger logger, String fmtString, Object... value) {
        if (StringUtils.isBlank(fmtString)) {
            return;
        }
        if (null != value && value.length != 0) {
            fmtString = String.format(fmtString, value);
        }
        error(logger, fmtString);
    }
}
