package com.mall.platform.enums;

/**
 * 商品上下架状态（与库字段 sale_status 取值一致）。
 */
public enum ProductSaleStatus {
    ON_SHELF,
    OFF_SHELF;

    public String getCode() {
        return name();
    }
}
