package com.mall.platform.common;

/**
 * 业务异常，支持自定义返回码。
 */
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
