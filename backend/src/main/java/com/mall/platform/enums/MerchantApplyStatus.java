package com.mall.platform.enums;

/**
 * 商家入驻申请状态（与库字段 apply_status 取值一致）。
 */
public enum MerchantApplyStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String getCode() {
        return name();
    }
}
