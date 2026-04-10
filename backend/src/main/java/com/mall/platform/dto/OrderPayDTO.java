package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 模拟支付请求参数。
 */
public class OrderPayDTO {
    @NotNull(message = "userId 不能为空")
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
