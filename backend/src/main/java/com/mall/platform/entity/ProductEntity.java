package com.mall.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体（MVP 单规格）。
 */
@TableName("product")
public class ProductEntity {
    @TableId("id")
    private Long id;

    @TableField("shop_id")
    private Long shopId;

    @TableField("product_sn")
    private String productSn;

    @TableField("product_name")
    private String productName;

    @TableField("product_subtitle")
    private String productSubtitle;

    @TableField("main_image")
    private String mainImage;

    @TableField("detail")
    private String detail;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock")
    private Integer stock;

    @TableField("sale_status")
    private String saleStatus;

    @TableField("audit_status")
    private String auditStatus;

    @TableField("is_deleted")
    private Boolean deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getProductSn() {
        return productSn;
    }

    public void setProductSn(String productSn) {
        this.productSn = productSn;
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

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
