package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.ShopEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 店铺数据访问层。
 */
@Mapper
public interface ShopRepository extends BaseMapper<ShopEntity> {
}
