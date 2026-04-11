package com.mall.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 商家修改商品参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 */
public class MerchantProductUpdateDTO {
    private Long userId;

    private Long merchantId;

    @NotBlank(message = "productName 不能为空")
    private String productName;

    private String productSubtitle;

    private String mainImage;

    private String detail;

    @NotNull(message = "price 不能为空")
    @DecimalMin(value = "0.00", message = "price 不能小于 0")
    private BigDecimal price;

    @NotNull(message = "stock 不能为空")
    @Min(value = 0, message = "stock 不能小于 0")
    private Integer stock;

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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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
}
