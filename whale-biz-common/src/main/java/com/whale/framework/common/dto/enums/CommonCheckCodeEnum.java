package com.whale.framework.common.dto.enums;

import com.whale.framework.common.utils.ValidationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 常用校验码枚举
 */
@Slf4j
public enum CommonCheckCodeEnum {
    PHONE("PHONE","电话号码"){
        @Override
        public boolean check(String value) {
            return ValidationUtil.isPhone(value);
        }
    },
    MOBILE_PHONE("MOBILE_PHONE","手机"){
        @Override
        public boolean check(String value) {
            return ValidationUtil.isMobile(value);
        }
    },
    EMAIL("EMAIL","邮箱") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isEmail(value);
        }
    },
    ID_CARD("IDENTITY_CARD","身份证") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isIdCard(value);
        }
    },
    URL("URL","URL地址") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isUrl(value);
        }
    },
    DATE_STRING_YYYY_MM_DD("DATE_STRING_YYYY_MM_DD","YYYY_MM_DD日期格式") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isYYYMMDDDate(value);
        }
    },

    IS_Y_N("IS_Y_N","Y或者N") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isY_OR_N(value);
        }
    },

    SIGN_TYPE("SIGN_TYPE","SIGN_TYPE") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isSIGN_TYPE(value);
        }
    },

    DOUBLE_POSITIVE_TWO_POINT("DOUBLE_POSITIVE_TWO_POINT","DOUBLE_POSITIVE_TWO_POINT") {
        @Override
        public boolean check(String value) {
            return ValidationUtil.isDOUBLE_POSITIVE_TWO_POINT(value);
        }
    };



    private String type;
    private String cName;

    public String getType() {
        return type;
    }

    public String getcName() {
        return cName;
    }

    CommonCheckCodeEnum(String type, String cName) {
        this.type = type;
        this.cName = cName;
    }

    public abstract boolean check(String value);


}
