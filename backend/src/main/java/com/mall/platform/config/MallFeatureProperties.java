package com.mall.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 多商户相关特性开关（默认全部关闭，不改变现有自营闭环行为）。
 * 配置路径：mall.feature.* ，见 application.yml。
 */
@ConfigurationProperties(prefix = "mall.feature")
public class MallFeatureProperties {

    /**
     * 是否启用多店铺展示/运营能力（当前未接 UI 与主流程）。
     */
    private boolean multiShop = false;

    /**
     * 是否以子订单（shop_order）为主进行拆单与状态流转（当前未接入创建订单/支付/发货主流程）。
     */
    private boolean splitOrder = false;

    /**
     * 是否启用分账/结算相关扩展（仅占位，无业务实现）。
     */
    private boolean settlement = false;

    public boolean isMultiShop() {
        return multiShop;
    }

    public void setMultiShop(boolean multiShop) {
        this.multiShop = multiShop;
    }

    public boolean isSplitOrder() {
        return splitOrder;
    }

    public void setSplitOrder(boolean splitOrder) {
        this.splitOrder = splitOrder;
    }

    public boolean isSettlement() {
        return settlement;
    }

    public void setSettlement(boolean settlement) {
        this.settlement = settlement;
    }
}
