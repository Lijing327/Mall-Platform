package com.mall.platform.config;

import org.springframework.stereotype.Component;

/**
 * 特性开关统一入口，避免在业务代码中散落硬编码布尔字面量。
 * 后续接入多店铺、拆单、分账时，仅在此处与配置中心交互即可。
 */
@Component
public class FeatureFlags {

    private final MallFeatureProperties mallFeatureProperties;

    public FeatureFlags(MallFeatureProperties mallFeatureProperties) {
        this.mallFeatureProperties = mallFeatureProperties;
    }

    /** 多店铺能力是否开启 */
    public boolean multiShopEnabled() {
        return mallFeatureProperties.isMultiShop();
    }

    /** 子订单拆单主流程是否开启 */
    public boolean splitOrderEnabled() {
        return mallFeatureProperties.isSplitOrder();
    }

    /** 分账/结算能力是否开启 */
    public boolean settlementEnabled() {
        return mallFeatureProperties.isSettlement();
    }
}
