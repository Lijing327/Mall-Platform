package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 商家入驻申请参数。
 */
public class MerchantApplyDTO {
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotBlank(message = "merchantName 不能为空")
    private String merchantName;

    @NotBlank(message = "contactName 不能为空")
    private String contactName;

    @NotBlank(message = "contactMobile 不能为空")
    private String contactMobile;

    /**
     * 资质文本描述（MVP 先不做文件上传）。
     */
    private String qualificationText;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getQualificationText() {
        return qualificationText;
    }

    public void setQualificationText(String qualificationText) {
        this.qualificationText = qualificationText;
    }
}
