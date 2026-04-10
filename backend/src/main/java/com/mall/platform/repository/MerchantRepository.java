package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.MerchantEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家数据访问层。
 */
@Mapper
public interface MerchantRepository extends BaseMapper<MerchantEntity> {
}
