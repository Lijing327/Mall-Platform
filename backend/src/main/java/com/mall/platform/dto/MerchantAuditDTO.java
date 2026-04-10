package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 商家审核参数。
 */
public class MerchantAuditDTO {
    @NotNull(message = "merchantId 不能为空")
    private Long merchantId;

    @NotBlank(message = "auditAction 不能为空")
    private String auditAction;

    /**
     * 驳回原因或审核备注。
     */
    private String auditRemark;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }
}
