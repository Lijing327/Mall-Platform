package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.OrderItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项数据访问层。
 */
@Mapper
public interface OrderItemRepository extends BaseMapper<OrderItemEntity> {
}
