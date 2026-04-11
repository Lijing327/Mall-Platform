package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.platform.entity.ShopOrderEntity;
import com.mall.platform.repository.ShopOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 子订单领域服务骨架。
 * 说明：创建订单、支付、发货等主流程当前仍不读写本表；后续在特性开关打开后再接入写入与状态流转。
 */
@Service
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;

    public ShopOrderService(ShopOrderRepository shopOrderRepository) {
        this.shopOrderRepository = shopOrderRepository;
    }

    /**
     * 按主单 ID 查询子订单列表（供未来接口组装展示；当前库中通常为空）。
     */
    public List<ShopOrderEntity> listByOrderId(Long orderId) {
        LambdaQueryWrapper<ShopOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrderEntity::getOrderId, orderId);
        wrapper.orderByAsc(ShopOrderEntity::getId);
        return shopOrderRepository.selectList(wrapper);
    }

    /**
     * 批量按主单 ID 查询子订单（用于我的订单列表组装）。
     */
    public List<ShopOrderEntity> listByOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ShopOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShopOrderEntity::getOrderId, orderIds);
        wrapper.orderByAsc(ShopOrderEntity::getOrderId).orderByAsc(ShopOrderEntity::getId);
        return shopOrderRepository.selectList(wrapper);
    }
}
