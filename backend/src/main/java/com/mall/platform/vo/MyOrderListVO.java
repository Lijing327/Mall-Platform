package com.mall.platform.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的订单 — 单条订单（含订单项列表）。
 */
public class MyOrderListVO {
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String payType;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private String receiverName;
    private String remark;
    private String shippingNo;
    private String shippingRemark;
    private LocalDateTime shipTime;
    private List<MyOrderItemVO> items = new ArrayList<>();

    /**
     * 子订单（按店铺）列表；单店默认模式下可由主单派生一条便于前端后续扩展。
     */
    private List<MyOrderShopOrderVO> shopOrders = new ArrayList<>();

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public List<MyOrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<MyOrderItemVO> items) {
        this.items = items;
    }

    public List<MyOrderShopOrderVO> getShopOrders() {
        return shopOrders;
    }

    public void setShopOrders(List<MyOrderShopOrderVO> shopOrders) {
        this.shopOrders = shopOrders != null ? shopOrders : new ArrayList<>();
    }
}
