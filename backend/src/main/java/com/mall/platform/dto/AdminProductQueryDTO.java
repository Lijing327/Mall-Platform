package com.mall.platform.dto;

/**
 * 管理员商品列表查询参数。
 */
public class AdminProductQueryDTO extends BasePageQueryDTO {
    private String keyword;
    private Long shopId;
    private String saleStatus;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }
}
