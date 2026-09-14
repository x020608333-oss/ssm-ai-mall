package com.shop.common;

/**
 * 业务异常：库存不足、重复用户名等，由全局异常处理器统一转成 Result 返回
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
