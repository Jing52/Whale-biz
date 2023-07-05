package com.whale.framework.common.dto.exception;

import lombok.Data;

/**
 * 基本校验异常
 */
@Data
public class BizServiceException extends RuntimeException {

    /**
     * 统一使用{@link }，不要在使用ClientResponseCode
     */
    private String code;

    private String errMsg;

    public BizServiceException(String errMsg) {
        this.errMsg = errMsg;
    }

    public BizServiceException(String code, String errMsg) {
          this.code = code;
          this.errMsg = errMsg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
