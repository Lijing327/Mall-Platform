package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.CartEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车数据访问层。
 */
@Mapper
public interface CartRepository extends BaseMapper<CartEntity> {
}
