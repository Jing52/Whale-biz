package com.whale.framework.common.utils;


import com.whale.framework.common.domain.enums.CommonCheckCodeEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.whale.framework.common.constants.RegularExpressionConstant.*;

/**
 * @Description: 验证工具
 * @Author Whale
 * @Date: 2023/6/27 10:12 AM
 */
public class ValidationUtil {

    /**
     * 判断字段是否为空 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static synchronized boolean strIsNull(String str) {
        return null == str || str.trim().length() <= 0 ? true : false;
    }

    /**
     * 判断字段是非空 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean strNonNull(String str) {
        return !strIsNull(str);
    }

    /**
     * 字符串null转空
     *
     * @param str
     * @return boolean
     */
    public static String nullToStr(String str) {
        return strIsNull(str) ? "" : str;
    }

    /**
     * 字符串null赋值默认值
     *
     * @param str    目标字符串
     * @param def 默认值
     * @return String
     */
    public static String nullToDefaultStr(String str, String def) {
        return strIsNull(str) ? def : str;
    }

    /**
     * 判断字段是否为Email 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isEmail(String str) {
        return Regular(str, EMAIL);
    }

    /**
     * 判断是否为电话号码 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isPhone(String str) {
        return Regular(str, PHONE);
    }

    /**
     * 判断是否为手机号码 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isMobile(String str) {
        return Regular(str, MOBILE);
    }

    /**
     * 判断是否为Url 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isUrl(String str) {
        return Regular(str, URL);
    }

    /**
     * 判断字段是否为DOUBLE 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDouble(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为数字 正负整数 正负浮点数 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isNumber(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为INTEGER  符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isInteger(String str) {
        return Regular(str, INTEGER);
    }

    /**
     * 判断字段是否为年龄 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isAge(String str) {
        return Regular(str, AGE);
    }

    /**
     * 判断字段是否为正整数正则表达式 >=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isINTEGER_NEGATIVE(String str) {
        return Regular(str, INTEGER_NEGATIVE);
    }

    /**
     * 判断字段是否为负整数正则表达式 <=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isINTEGER_POSITIVE(String str) {
        return Regular(str, INTEGER_POSITIVE);
    }

    /**
     * 判断字段是否为正浮点数正则表达式 >=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDOUBLE_NEGATIVE(String str) {
        return Regular(str, DOUBLE_NEGATIVE);
    }

    /**
     * 判断字段是否为负浮点数正则表达式 <=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDOUBLE_POSITIVE(String str) {
        return Regular(str, DOUBLE_POSITIVE);
    }

    /**
     * 判断字段是否为日期 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDate(String str) {
        return Regular(str, DATE_ALL);
    }

    /**
     * 验证2010-12-10
     *
     * @param str
     * @return
     */
    public static boolean isYYYMMDDDate(String str) {
        return Regular(str, DATE_FORMAT_YYY_MM_DD);
    }

    /**
     * 判断字段是否超长
     * 字串为空返回fasle, 超过长度{leng}返回ture 反之返回false
     *
     * @param str
     * @param len
     * @return boolean
     */
    public static boolean isLengthOut(String str, int len) {
        return strIsNull(str) ? false : str.trim().length() > len;
    }

    /**
     * 判断字段是否为身份证 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isIdCard(String str) {
        if (strIsNull(str)) {
            return false;
        }
        if (str.trim().length() == 15 || str.trim().length() == 18) {
            return Regular(str, IDCARD);
        } else {
            return false;
        }

    }

    /**
     * 判断字段是否为邮编 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isCode(String str) {
        return Regular(str, CODE);
    }

    /**
     * 判断字符串是不是全部是英文字母
     *
     * @param str
     * @return boolean
     */
    public static boolean isEnglish(String str) {
        return Regular(str, STR_ENG);
    }

    /**
     * 判断字符串是不是全部是英文字母+数字
     *
     * @param str
     * @return boolean
     */
    public static boolean isENG_NUM(String str) {
        return Regular(str, STR_ENG_NUM);
    }

    /**
     * 判断字符串是不是全部是英文字母+数字+下划线
     *
     * @param str
     * @return boolean
     */
    public static boolean isENG_NUM_(String str) {
        return Regular(str, STR_ENG_NUM_);
    }

    /**
     * 过滤特殊字符串 返回过滤后的字符串
     *
     * @param str
     * @return boolean
     */
    public static String filterSpecialStr(String str) {
        Pattern p = Pattern.compile(STR_SPECIAL);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 校验机构代码格式
     *
     * @return
     */
    public static boolean isOrgCode(String str) {
        return Regular(str, ORG_CODE);
    }

    /**
     * 判断字符串是不是数字组成
     *
     * @param str
     * @return boolean
     */
    public static boolean isSTR_NUM(String str) {
        return Regular(str, STR_NUM);
    }


    public static boolean isY_OR_N(String str) {
        return Regular(str, Y_OR_N);
    }

    /**
     * 大于0，且不超过两位小数
     **/
    public static boolean isSIGN_TYPE(String str) {
        return Regular(str, isSIGN_TYPE);
    }

    public static boolean isDOUBLE_POSITIVE_TWO_POINT(String str) {
        return Regular(str, DOUBLE_POSITIVE_TWO_POINT);
    }

    /**
     * 匹配是否符合正则表达式pattern 匹配返回true
     *
     * @param str     匹配的字符串
     * @param pattern 匹配模式
     * @return boolean
     */
    private static boolean Regular(String str, String pattern) {
        if (null == str || str.trim().length() <= 0) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static void main(String args[]) {

        System.out.println("YYYY:" + CommonCheckCodeEnum.IS_Y_N.check("Y"));
        System.out.println("NNNNNN" + CommonCheckCodeEnum.IS_Y_N.check("N"));
        System.out.println("     " + CommonCheckCodeEnum.IS_Y_N.check(""));
        System.out.println("null    " + CommonCheckCodeEnum.IS_Y_N.check(null));

        System.out.println("============= 分割线 ===============");

        System.out.println("TRANSFER_SIGN:" + CommonCheckCodeEnum.SIGN_TYPE.check("TRANSFER_SIGN"));
        System.out.println("Nfsd" + CommonCheckCodeEnum.SIGN_TYPE.check("Nfsd"));
        System.out.println("     " + CommonCheckCodeEnum.SIGN_TYPE.check(""));
        System.out.println("null    " + CommonCheckCodeEnum.SIGN_TYPE.check(null));

        System.out.println("============= 分割线 ===============");

        System.out.println("0.99:" + CommonCheckCodeEnum.DOUBLE_POSITIVE_TWO_POINT.check("0.99"));
        System.out.println("10000" + CommonCheckCodeEnum.DOUBLE_POSITIVE_TWO_POINT.check("10000"));
        System.out.println("0.9999     " + CommonCheckCodeEnum.DOUBLE_POSITIVE_TWO_POINT.check("0.9999"));
        System.out.println("-0.6    " + CommonCheckCodeEnum.DOUBLE_POSITIVE_TWO_POINT.check("-0.6"));
        System.out.println("0.6    " + CommonCheckCodeEnum.DOUBLE_POSITIVE_TWO_POINT.check("0.6"));
    }
}