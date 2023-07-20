package com.whale.framework.common.domain.response;

import java.io.Serializable;

/**
 * 请求信息
 */
public class BaseResponse<T> implements Serializable {
    private String code = ResponseCodeEnum.SUCCESS.getCode();
    private String msg = ResponseCodeEnum.SUCCESS.getMsg();
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> resp = new BaseResponse<T>();
        resp.setCode(ResponseCodeEnum.SUCCESS.getCode());
        resp.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
        resp.setData(data);
        return resp;
    }

    public static <T> BaseResponse<T> fail(ResponseCodeEnum responseCode) {
        BaseResponse<T> resp = new BaseResponse<T>();
        resp.setCode(responseCode.getCode());
        resp.setMsg(responseCode.getMsg());
        return resp;
    }

    public static <T> BaseResponse<T> fail(String code, String msg) {
        BaseResponse<T> resp = new BaseResponse<T>();
        resp.setCode(code);
        resp.setMsg(msg);
        return resp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
