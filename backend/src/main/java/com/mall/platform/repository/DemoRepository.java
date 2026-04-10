package com.mall.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.platform.entity.DemoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 演示仓储接口：用于体现 repository 分层。
 */
@Mapper
public interface DemoRepository extends BaseMapper<DemoEntity> {
}
