package com.mall.platform.dto;

/**
 * 创建订单请求参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 */
public class OrderCreateDTO {
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
