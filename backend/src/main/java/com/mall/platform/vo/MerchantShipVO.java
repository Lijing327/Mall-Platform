package com.mall.platform.vo;

/**
 * 商家发货返回对象。
 */
public class MerchantShipVO {
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private String shippingNo;
    private String shippingRemark;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getShippingNo() {
        return shippingNo;
    }

    public void setShippingNo(String shippingNo) {
        this.shippingNo = shippingNo;
    }

    public String getShippingRemark() {
        return shippingRemark;
    }

    public void setShippingRemark(String shippingRemark) {
        this.shippingRemark = shippingRemark;
    }
}
