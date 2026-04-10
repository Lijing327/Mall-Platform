package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单主表数据访问层。
 */
@Mapper
public interface OrderRepository extends BaseMapper<OrderEntity> {
}
