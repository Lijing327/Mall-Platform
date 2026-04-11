package com.mall.platform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 * addressId 为当前用户选择的收货地址主键。
 */
public class OrderCreateDTO {
    private Long userId;

    @NotNull(message = "addressId 不能为空，请先选择收货地址")
    private Long addressId;

    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
