package com.whale.framework.common.utils;

import com.whale.framework.common.domain.exception.BizServiceException;
import com.whale.framework.common.domain.response.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.whale.framework.common.domain.response.ResponseCodeEnum.BIZ_BUSINESS_EXCEPTION;


/**
 * @Description: 断言工具
 * @Author Whale
 * @Date: 2023/6/27 10:12 AM
 */
@Slf4j
public class AssertUtil {

    /**
     * 断言表达式的值为true，否则抛BizServiceException
     *
     * @param expValue 断言表达式的值
     * @param errMsg   异常错误码
     * @throws BizServiceException
     */
    public static void assertTrue(boolean expValue, String errMsg) throws BizServiceException {
        if (!expValue) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    public static void assertTrue(boolean expValue, String code, String errMsg) throws BizServiceException {
        if (!expValue) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(code, errMsg);
        }
    }


    /**
     * 断言表达式的值为false，否则抛BizServiceException
     *
     * @param expValue 断言表达式的值
     * @param errCode  异常错误码
     * @param errMsg   异常描述
     * @throws BizServiceException
     */
    public static void assertFalse(boolean expValue, String errCode,
                                   String errMsg) throws BizServiceException {
        if (expValue) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }

    public static void assertFalse(boolean excepValue,
                                   String errMsg) throws BizServiceException {
        if (excepValue) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    public static void assertFalse(boolean expValue, ResponseCodeEnum errCode,
                                   String errMsg) throws BizServiceException {
        if (expValue) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode.getCode(), errMsg);
        }
    }

    /**
     * 断言两个对象相等，否则抛BizServiceException
     *
     * @param obj1   A对象
     * @param obj2   B对象
     * @param errMsg 异常错误
     * @throws BizServiceException
     */
    public static void assertEquals(Object obj1, Object obj2,
                                    String errMsg) throws BizServiceException {
        if (obj1 == null) {
            assertNull(obj2, errMsg);
            return;
        }

        if (!obj1.equals(obj2)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    /**
     * 断言两个对象相等，否则抛BizServiceException
     *
     * @param obj1    A对象
     * @param obj2    B对象
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertEquals(Object obj1, Object obj2, String errCode,
                                    String errMsg) throws BizServiceException {
        if (obj1 == null) {
            assertNull(obj2, errCode, errMsg);
            return;
        }

        if (!obj1.equals(obj2)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    /**
     * 断言两个对象不等，否则抛BizServiceException
     *
     * @param obj1    A对象
     * @param obj2    B对象
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertNotEquals(Object obj1, Object obj2, String errCode,
                                       String errMsg) throws BizServiceException {
        if (obj1 == null) {
            assertNotNull(obj2, errCode, errMsg);
            return;
        }

        if (obj1.equals(obj2)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    /**
     * 断言字符串为空，否则抛BizServiceException
     *
     * @param str     断言字符串
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertEmpty(String str, String errCode,
                                   String errMsg) throws BizServiceException {
        if (StringUtils.isNotEmpty(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    /**
     * 断言字符串为非空，否则抛BizServiceException
     *
     * @param str     断言字符串
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertNotEmpty(String str, String errCode,
                                      String errMsg) throws BizServiceException {
        if (StringUtils.isEmpty(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }

    public static void assertNotEmpty(String str,
                                      String errMsg) throws BizServiceException {
        if (StringUtils.isEmpty(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }


    /**
     * 断言字符串为空，否则抛BizServiceException
     *
     * @param str     断言字符串
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertBlank(String str, String errCode,
                                   String errMsg) throws BizServiceException {
        if (StringUtils.isNotBlank(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    /**
     * 断言字符串为非空，否则抛BizServiceException
     *
     * @param str    断言字符串
     * @param errMsg 异常错
     * @throws BizServiceException
     */
    public static void assertNotBlank(String str, String errMsg) throws BizServiceException {
        if (StringUtils.isBlank(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    /**
     * 断言字符串为非空，否则抛BizServiceException
     *
     * @param str     断言字符串
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertNotBlank(String str, String errCode,
                                      String errMsg) throws BizServiceException {
        if (StringUtils.isBlank(str)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    /**
     * 断言对象为null，否则抛BizServiceException
     *
     * @param object 断言对象
     * @param errMsg 异常错误
     * @throws BizServiceException
     */
    public static void assertNull(Object object, String errMsg) throws BizServiceException {
        if (object != null) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    /**
     * 断言对象为null，否则抛BizServiceException
     *
     * @param object  断言对象
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertNull(Object object, String errCode,
                                  String errMsg) throws BizServiceException {
        if (object != null) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }

    /**
     * 断言对象非null，否则抛BizServiceException
     *
     * @param object  断言对象
     * @param errCode 异常错误码
     * @param errMsg  异常描述
     * @throws BizServiceException
     */
    public static void assertNotNull(Object object, String errCode,
                                     String errMsg) throws BizServiceException {
        if (null == object) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }

    public static void assertNotNull(Object object, ResponseCodeEnum errCode,
                                     String errMsg) throws BizServiceException {
        if (null == object) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode.getCode(), errMsg);
        }
    }

    /**
     * 断言对象非null，否则抛BizServiceException
     *
     * @param object 断言对象
     * @param errMsg 异常错误
     * @throws BizServiceException
     */
    public static void assertNotNull(Object object, String errMsg) throws BizServiceException {
        if (null == object) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

    /**
     * 断言集合不为空或null，否则抛BizServiceException
     *
     * @param collection 断言集合
     * @param errCode    异常错误码
     * @param errMsg     异常描述
     * @throws BizServiceException
     */
    @SuppressWarnings("rawtypes")
    public static void assertNotBlank(Collection collection, String errCode,
                                      String errMsg) throws BizServiceException {
        if (CollectionUtils.isEmpty(collection)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(errCode, errMsg);
        }
    }


    public static void assertIsDate(String sDate, String pattern, String errMsg) throws BizServiceException {
        AssertUtil.assertNotBlank(sDate, errMsg);
        Date date = DateUtil.parseDate(sDate, pattern);
        AssertUtil.assertNotNull(date, "日期格式非法");
    }

    public static void assertNotEmpty(Map<String, String> map,
                                      String errMsg) throws BizServiceException {
        if (MapUtils.isEmpty(map)) {
            LoggerUtils.fmtInfo(log, errMsg);
            throw new BizServiceException(BIZ_BUSINESS_EXCEPTION.getCode(), errMsg);
        }
    }

}