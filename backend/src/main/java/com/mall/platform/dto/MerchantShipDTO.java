package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 商家发货参数。
 */
public class MerchantShipDTO {
    @NotNull(message = "userId 不能为空")
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
