package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.AdminMerchantQueryDTO;
import com.mall.platform.dto.AdminOrderQueryDTO;
import com.mall.platform.dto.AdminProductQueryDTO;
import com.mall.platform.entity.MerchantEntity;
import com.mall.platform.entity.OrderEntity;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.repository.MerchantRepository;
import com.mall.platform.repository.OrderRepository;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.vo.AdminMerchantVO;
import com.mall.platform.vo.AdminOrderVO;
import com.mall.platform.vo.AdminProductVO;
import com.mall.platform.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminService(MerchantRepository merchantRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        this.merchantRepository = merchantRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * 管理员查询商家申请（分页）。
     */
    public PageVO<AdminMerchantVO> listMerchants(AdminMerchantQueryDTO queryDTO) {
        Integer pageNum = normalizePageNum(queryDTO.getPageNum());
        Integer pageSize = normalizePageSize(queryDTO.getPageSize());

        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getApplyStatus())) {
            queryWrapper.eq(MerchantEntity::getApplyStatus, queryDTO.getApplyStatus().trim().toUpperCase());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(MerchantEntity::getMerchantName, queryDTO.getKeyword().trim());
        }
        queryWrapper.orderByDesc(MerchantEntity::getCreateTime);

        Page<MerchantEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<MerchantEntity> page = merchantRepository.selectPage(pageRequest, queryWrapper);
        List<AdminMerchantVO> list = new ArrayList<>();
        for (MerchantEntity entity : page.getRecords()) {
            AdminMerchantVO vo = new AdminMerchantVO();
            vo.setMerchantId(entity.getId());
            vo.setUserId(entity.getUserId());
            vo.setMerchantCode(entity.getMerchantCode());
            vo.setMerchantName(entity.getMerchantName());
            vo.setContactName(entity.getContactName());
            vo.setContactMobile(entity.getContactMobile());
            vo.setApplyStatus(entity.getApplyStatus());
            vo.setAuditRemark(entity.getAuditRemark());
            vo.setApplyTime(entity.getApplyTime());
            vo.setAuditTime(entity.getAuditTime());
            list.add(vo);
        }

        return toPageVO(page, list);
    }

    /**
     * 管理员查询订单（分页）。
     */
    public PageVO<AdminOrderVO> listOrders(AdminOrderQueryDTO queryDTO) {
        Integer pageNum = normalizePageNum(queryDTO.getPageNum());
        Integer pageSize = normalizePageSize(queryDTO.getPageSize());

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getOrderStatus())) {
            queryWrapper.eq(OrderEntity::getOrderStatus, queryDTO.getOrderStatus().trim().toUpperCase());
        }
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            queryWrapper.like(OrderEntity::getOrderNo, queryDTO.getOrderNo().trim());
        }
        queryWrapper.orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<OrderEntity> page = orderRepository.selectPage(pageRequest, queryWrapper);
        List<AdminOrderVO> list = new ArrayList<>();
        for (OrderEntity entity : page.getRecords()) {
            AdminOrderVO vo = new AdminOrderVO();
            vo.setOrderId(entity.getId());
            vo.setOrderNo(entity.getOrderNo());
            vo.setUserId(entity.getUserId());
            vo.setOrderStatus(entity.getOrderStatus());
            vo.setTotalAmount(entity.getTotalAmount());
            vo.setPayAmount(entity.getPayAmount());
            vo.setPayType(entity.getPayType());
            vo.setPayTime(entity.getPayTime());
            vo.setCreateTime(entity.getCreateTime());
            list.add(vo);
        }

        return toPageVO(page, list);
    }

    /**
     * 管理员查询商品（分页）。
     */
    public PageVO<AdminProductVO> listProducts(AdminProductQueryDTO queryDTO) {
        Integer pageNum = normalizePageNum(queryDTO.getPageNum());
        Integer pageSize = normalizePageSize(queryDTO.getPageSize());

        LambdaQueryWrapper<ProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(ProductEntity::getProductName, queryDTO.getKeyword().trim());
        }
        if (queryDTO.getShopId() != null) {
            queryWrapper.eq(ProductEntity::getShopId, queryDTO.getShopId());
        }
        if (StringUtils.hasText(queryDTO.getSaleStatus())) {
            queryWrapper.eq(ProductEntity::getSaleStatus, queryDTO.getSaleStatus().trim().toUpperCase());
        }
        queryWrapper.orderByDesc(ProductEntity::getCreateTime);

        Page<ProductEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<ProductEntity> page = productRepository.selectPage(pageRequest, queryWrapper);
        List<AdminProductVO> list = new ArrayList<>();
        for (ProductEntity entity : page.getRecords()) {
            AdminProductVO vo = new AdminProductVO();
            vo.setProductId(entity.getId());
            vo.setShopId(entity.getShopId());
            vo.setProductSn(entity.getProductSn());
            vo.setProductName(entity.getProductName());
            vo.setPrice(entity.getPrice());
            vo.setStock(entity.getStock());
            vo.setSaleStatus(entity.getSaleStatus());
            vo.setAuditStatus(entity.getAuditStatus());
            vo.setDeleted(entity.getDeleted());
            vo.setCreateTime(entity.getCreateTime());
            list.add(vo);
        }

        return toPageVO(page, list);
    }

    /**
     * 管理员下架商品（预留最小能力）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void offShelfProduct(Long productId) {
        ProductEntity entity = productRepository.selectById(productId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        entity.setSaleStatus("OFF_SHELF");
        productRepository.updateById(entity);
    }

    private Integer normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 || pageSize > 100 ? 10 : pageSize;
    }

    private <T> PageVO<T> toPageVO(IPage<?> page, List<T> list) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(page.getCurrent());
        pageVO.setPageSize(page.getSize());
        pageVO.setList(list);
        return pageVO;
    }
}
