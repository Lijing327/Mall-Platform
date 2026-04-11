package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 子订单发货请求体（预留；当前服务端会明确拒绝并提示使用主单发货接口）。
 */
public class MerchantShopShipDTO {

    private Long userId;

    private Long merchantId;

    @NotBlank(message = "shippingNo 不能为空")
    private String shippingNo;

    private String shippingRemark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getShippingNo() {
        return shippingNo;
    }

    public void setShippingNo(String shippingNo) {
        this.shippingNo = shippingNo;
    }

    public String getShippingRemark() {
        return shippingRemark;
    }

    public void setShippingRemark(String shippingRemark) {
        this.shippingRemark = shippingRemark;
    }
}
