package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.UserAddressSaveDTO;
import com.mall.platform.entity.UserAddressEntity;
import com.mall.platform.repository.UserAddressRepository;
import com.mall.platform.vo.UserAddressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户收货地址服务：CRUD + 默认地址管理。
 */
@Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    public UserAddressService(UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    /**
     * 列表：未删除的地址，默认地址在最前，其次按创建时间倒序。
     */
    public List<UserAddressVO> listByUserId(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddressEntity::getUserId, userId);
        wrapper.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        wrapper.orderByDesc(UserAddressEntity::getIsDefault);
        wrapper.orderByDesc(UserAddressEntity::getCreateTime);
        List<UserAddressEntity> list = userAddressRepository.selectList(wrapper);
        List<UserAddressVO> vos = new ArrayList<>();
        for (UserAddressEntity entity : list) {
            vos.add(toVO(entity));
        }
        return vos;
    }

    /**
     * 获取当前用户的默认地址；无默认地址返回 null。
     */
    public UserAddressVO getDefault(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddressEntity::getUserId, userId);
        wrapper.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        wrapper.eq(UserAddressEntity::getIsDefault, Boolean.TRUE);
        wrapper.last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        UserAddressEntity entity = userAddressRepository.selectOne(wrapper);
        return entity == null ? null : toVO(entity);
    }

    /**
     * 创建地址：若为首条地址或显式设为默认，则记为默认并清除其它默认位。
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO create(UserAddressSaveDTO dto) {
        boolean shouldBeDefault = Boolean.TRUE.equals(dto.getIsDefault()) || !hasAnyAddress(dto.getUserId());
        if (shouldBeDefault) {
            clearDefault(dto.getUserId());
        }
        UserAddressEntity entity = new UserAddressEntity();
        entity.setUserId(dto.getUserId());
        entity.setReceiverName(dto.getReceiverName());
        entity.setReceiverMobile(dto.getReceiverMobile());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setDetailAddress(dto.getDetailAddress());
        entity.setIsDefault(shouldBeDefault);
        entity.setDeleted(Boolean.FALSE);
        userAddressRepository.insert(entity);
        return toVO(entity);
    }

    /**
     * 修改地址：仅限本人；可顺带切换默认。
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO update(Long addressId, UserAddressSaveDTO dto) {
        UserAddressEntity entity = requireOwn(addressId, dto.getUserId());
        entity.setReceiverName(dto.getReceiverName());
        entity.setReceiverMobile(dto.getReceiverMobile());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setDetailAddress(dto.getDetailAddress());
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefault(dto.getUserId());
            entity.setIsDefault(Boolean.TRUE);
        }
        userAddressRepository.updateById(entity);
        return toVO(entity);
    }

    /**
     * 删除（软删）：若删除的是默认地址，剩余地址中最近一条自动成为新默认。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long addressId, Long userId) {
        UserAddressEntity entity = requireOwn(addressId, userId);
        entity.setDeleted(Boolean.TRUE);
        entity.setIsDefault(Boolean.FALSE);
        userAddressRepository.updateById(entity);

        if (!hasDefault(userId)) {
            UserAddressEntity latest = findLatest(userId);
            if (latest != null) {
                latest.setIsDefault(Boolean.TRUE);
                userAddressRepository.updateById(latest);
            }
        }
    }

    /**
     * 设为默认：清除当前用户其他默认，再置当前地址为默认。
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO setAsDefault(Long addressId, Long userId) {
        UserAddressEntity entity = requireOwn(addressId, userId);
        clearDefault(userId);
        entity.setIsDefault(Boolean.TRUE);
        userAddressRepository.updateById(entity);
        return toVO(entity);
    }

    /**
     * 查询一条属于当前用户的地址实体；用于下单快照。
     */
    public UserAddressEntity findOwnEntity(Long addressId, Long userId) {
        UserAddressEntity entity = userAddressRepository.selectById(addressId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted()) || !userId.equals(entity.getUserId())) {
            return null;
        }
        return entity;
    }

    private UserAddressEntity requireOwn(Long addressId, Long userId) {
        UserAddressEntity entity = findOwnEntity(addressId, userId);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "收货地址不存在");
        }
        return entity;
    }

    private boolean hasAnyAddress(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddressEntity::getUserId, userId);
        wrapper.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        return userAddressRepository.selectCount(wrapper) > 0;
    }

    private boolean hasDefault(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddressEntity::getUserId, userId);
        wrapper.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        wrapper.eq(UserAddressEntity::getIsDefault, Boolean.TRUE);
        return userAddressRepository.selectCount(wrapper) > 0;
    }

    private UserAddressEntity findLatest(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddressEntity::getUserId, userId);
        wrapper.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        wrapper.orderByDesc(UserAddressEntity::getCreateTime);
        wrapper.last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        return userAddressRepository.selectOne(wrapper);
    }

    private void clearDefault(Long userId) {
        LambdaUpdateWrapper<UserAddressEntity> update = new LambdaUpdateWrapper<>();
        update.eq(UserAddressEntity::getUserId, userId);
        update.eq(UserAddressEntity::getDeleted, Boolean.FALSE);
        update.eq(UserAddressEntity::getIsDefault, Boolean.TRUE);
        update.set(UserAddressEntity::getIsDefault, Boolean.FALSE);
        userAddressRepository.update(null, update);
    }

    private static UserAddressVO toVO(UserAddressEntity entity) {
        UserAddressVO vo = new UserAddressVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setReceiverName(entity.getReceiverName());
        vo.setReceiverMobile(entity.getReceiverMobile());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setDetailAddress(entity.getDetailAddress());
        vo.setIsDefault(entity.getIsDefault());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
