package com.mall.platform.dto;

/**
 * 管理员订单列表查询参数。
 */
public class AdminOrderQueryDTO extends BasePageQueryDTO {
    private String orderStatus;
    private String orderNo;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
