package com.mall.platform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 删除购物车项请求参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 */
public class CartDeleteDTO {
    private Long userId;

    @NotNull(message = "cartId 不能为空")
    private Long cartId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}
