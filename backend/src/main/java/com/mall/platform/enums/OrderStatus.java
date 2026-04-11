package com.mall.platform.enums;

/**
 * 订单状态（与库字段 order_status 取值一致）。
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    COMPLETED;

    public String getCode() {
        return name();
    }
}
