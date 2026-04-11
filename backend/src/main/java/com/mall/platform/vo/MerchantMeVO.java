package com.mall.platform.vo;

/**
 * 当前登录用户关联的商家信息（无记录时表示未申请）。
 */
public class MerchantMeVO {
    private Long merchantId;
    private String merchantName;
    private String applyStatus;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }
}
