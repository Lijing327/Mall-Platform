package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层。
 */
@Mapper
public interface ProductRepository extends BaseMapper<ProductEntity> {
}
