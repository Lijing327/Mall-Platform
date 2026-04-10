package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.MerchantProductCreateDTO;
import com.mall.platform.dto.MerchantProductQueryDTO;
import com.mall.platform.dto.MerchantProductUpdateDTO;
import com.mall.platform.entity.MerchantEntity;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.repository.MerchantRepository;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.MerchantProductVO;
import com.mall.platform.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MerchantProductService {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public MerchantProductService(MerchantRepository merchantRepository,
                                  ShopRepository shopRepository,
                                  ProductRepository productRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    /**
     * 商家查看自己店铺商品列表（分页）。
     */
    public PageVO<MerchantProductVO> listProducts(MerchantProductQueryDTO queryDTO) {
        ShopEntity shopEntity = resolveMerchantShop(queryDTO.getUserId(), queryDTO.getMerchantId());

        Integer pageNum = queryDTO.getPageNum();
        Integer pageSize = queryDTO.getPageSize();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        LambdaQueryWrapper<ProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductEntity::getShopId, shopEntity.getId());
        queryWrapper.eq(ProductEntity::getDeleted, Boolean.FALSE);
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(ProductEntity::getProductName, queryDTO.getKeyword().trim());
        }
        queryWrapper.orderByDesc(ProductEntity::getCreateTime);

        Page<ProductEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<ProductEntity> page = productRepository.selectPage(pageRequest, queryWrapper);
        List<MerchantProductVO> list = new ArrayList<>();
        for (ProductEntity entity : page.getRecords()) {
            list.add(toMerchantProductVO(entity));
        }

        PageVO<MerchantProductVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(page.getCurrent());
        pageVO.setPageSize(page.getSize());
        pageVO.setList(list);
        return pageVO;
    }

    /**
     * 商家新增商品（默认下架状态）。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantProductVO createProduct(MerchantProductCreateDTO createDTO) {
        ShopEntity shopEntity = resolveMerchantShop(createDTO.getUserId(), createDTO.getMerchantId());
        ProductEntity entity = new ProductEntity();
        entity.setShopId(shopEntity.getId());
        entity.setProductSn(generateProductSn(shopEntity.getId()));
        entity.setProductName(createDTO.getProductName());
        entity.setProductSubtitle(createDTO.getProductSubtitle());
        entity.setMainImage(createDTO.getMainImage());
        entity.setDetail(createDTO.getDetail());
        entity.setPrice(createDTO.getPrice());
        entity.setStock(createDTO.getStock());
        entity.setSaleStatus("OFF_SHELF");
        entity.setAuditStatus("PASS");
        entity.setDeleted(Boolean.FALSE);
        productRepository.insert(entity);
        return toMerchantProductVO(entity);
    }

    /**
     * 商家修改商品基础信息。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantProductVO updateProduct(Long productId, MerchantProductUpdateDTO updateDTO) {
        ShopEntity shopEntity = resolveMerchantShop(updateDTO.getUserId(), updateDTO.getMerchantId());
        ProductEntity entity = requireMerchantOwnProduct(productId, shopEntity.getId());

        entity.setProductName(updateDTO.getProductName());
        entity.setProductSubtitle(updateDTO.getProductSubtitle());
        entity.setMainImage(updateDTO.getMainImage());
        entity.setDetail(updateDTO.getDetail());
        entity.setPrice(updateDTO.getPrice());
        entity.setStock(updateDTO.getStock());
        productRepository.updateById(entity);
        return toMerchantProductVO(entity);
    }

    /**
     * 商家上架商品。
     */
    @Transactional(rollbackFor = Exception.class)
    public void onShelf(Long productId, Long userId, Long merchantId) {
        ShopEntity shopEntity = resolveMerchantShop(userId, merchantId);
        ProductEntity entity = requireMerchantOwnProduct(productId, shopEntity.getId());
        entity.setSaleStatus("ON_SHELF");
        productRepository.updateById(entity);
    }

    /**
     * 商家下架商品。
     */
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(Long productId, Long userId, Long merchantId) {
        ShopEntity shopEntity = resolveMerchantShop(userId, merchantId);
        ProductEntity entity = requireMerchantOwnProduct(productId, shopEntity.getId());
        entity.setSaleStatus("OFF_SHELF");
        productRepository.updateById(entity);
    }

    /**
     * 商家删除商品（逻辑删除）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId, Long userId, Long merchantId) {
        ShopEntity shopEntity = resolveMerchantShop(userId, merchantId);
        ProductEntity entity = requireMerchantOwnProduct(productId, shopEntity.getId());
        entity.setDeleted(Boolean.TRUE);
        entity.setSaleStatus("OFF_SHELF");
        productRepository.updateById(entity);
    }

    private ShopEntity resolveMerchantShop(Long userId, Long merchantId) {
        if (userId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "userId 不能为空");
        }
        LambdaQueryWrapper<MerchantEntity> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(MerchantEntity::getUserId, userId);
        if (merchantId != null) {
            merchantWrapper.eq(MerchantEntity::getId, merchantId);
        }
        MerchantEntity merchantEntity = merchantRepository.selectOne(merchantWrapper);
        if (merchantEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前用户不是商家或商家不存在");
        }
        if (!"APPROVED".equals(merchantEntity.getApplyStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商家审核未通过，不能操作商品");
        }

        LambdaQueryWrapper<ShopEntity> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(ShopEntity::getMerchantId, merchantEntity.getId());
        shopWrapper.eq(ShopEntity::getShopType, "MERCHANT");
        ShopEntity shopEntity = shopRepository.selectOne(shopWrapper);
        if (shopEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商家店铺不存在");
        }
        return shopEntity;
    }

    private ProductEntity requireMerchantOwnProduct(Long productId, Long shopId) {
        ProductEntity entity = productRepository.selectById(productId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        if (!shopId.equals(entity.getShopId())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "无权操作非本店商品");
        }
        return entity;
    }

    private MerchantProductVO toMerchantProductVO(ProductEntity entity) {
        MerchantProductVO vo = new MerchantProductVO();
        vo.setId(entity.getId());
        vo.setShopId(entity.getShopId());
        vo.setProductSn(entity.getProductSn());
        vo.setProductName(entity.getProductName());
        vo.setProductSubtitle(entity.getProductSubtitle());
        vo.setMainImage(entity.getMainImage());
        vo.setPrice(entity.getPrice());
        vo.setStock(entity.getStock());
        vo.setSaleStatus(entity.getSaleStatus());
        vo.setAuditStatus(entity.getAuditStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String generateProductSn(Long shopId) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "P" + shopId + timePart + randomPart;
    }
}
