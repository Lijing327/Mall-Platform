package com.mall.platform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求参数。
 */
public class OrderCreateDTO {
    @NotNull(message = "userId 不能为空")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
