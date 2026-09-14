package com.shop.common;

import lombok.Data;

/**
 * 统一接口返回结果
 */
@Data
public class Result<T> {
    /** 200成功 401未登录 403无权限 400参数错误 500服务器错误 */
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return build(200, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return build(200, "success", data);
    }

    public static <T> Result<T> error(String msg) {
        return build(500, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return build(code, msg, null);
    }

    public static <T> Result<T> build(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
