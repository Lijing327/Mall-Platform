package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 模拟支付请求参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 */
public class OrderPayDTO {
    private Long userId;

    @NotBlank(message = "orderNo 不能为空")
    private String orderNo;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
