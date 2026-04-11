package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.ShopOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 子订单表数据访问层（多店铺扩展预留）。
 */
@Mapper
public interface ShopOrderRepository extends BaseMapper<ShopOrderEntity> {
}
