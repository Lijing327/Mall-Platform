package com.mall.platform.vo;

/**
 * 商品中嵌套展示的店铺简要信息。
 */
public class ShopSimpleVO {
    private Long shopId;
    private String shopName;
    private String shopType;

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopType() {
        return shopType;
    }

    public void setShopType(String shopType) {
        this.shopType = shopType;
    }
}
