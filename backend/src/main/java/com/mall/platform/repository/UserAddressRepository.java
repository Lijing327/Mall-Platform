package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.UserAddressEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址数据访问层。
 */
@Mapper
public interface UserAddressRepository extends BaseMapper<UserAddressEntity> {
}
