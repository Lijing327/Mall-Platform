package com.mall.platform.vo;

import java.math.BigDecimal;

/**
 * 商品列表项返回对象。
 */
public class ProductListItemVO {
    private Long id;
    private String productName;
    private String productSubtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private ShopSimpleVO shop;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSubtitle() {
        return productSubtitle;
    }

    public void setProductSubtitle(String productSubtitle) {
        this.productSubtitle = productSubtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public ShopSimpleVO getShop() {
        return shop;
    }

    public void setShop(ShopSimpleVO shop) {
        this.shop = shop;
    }
}
