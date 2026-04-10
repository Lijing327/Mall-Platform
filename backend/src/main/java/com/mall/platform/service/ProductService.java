package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.ProductQueryDTO;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.PageVO;
import com.mall.platform.vo.ProductDetailVO;
import com.mall.platform.vo.ProductListItemVO;
import com.mall.platform.vo.ShopSimpleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public ProductService(ProductRepository productRepository, ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 商品分页列表查询（仅返回已上架商品）。
     */
    public PageVO<ProductListItemVO> listProducts(ProductQueryDTO queryDTO) {
        Integer pageNum = queryDTO.getPageNum();
        Integer pageSize = queryDTO.getPageSize();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        LambdaQueryWrapper<ProductEntity> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(ProductEntity::getSaleStatus, "ON_SHELF");
        productQueryWrapper.eq(ProductEntity::getDeleted, Boolean.FALSE);
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            productQueryWrapper.like(ProductEntity::getProductName, queryDTO.getKeyword().trim());
        }
        if (queryDTO.getShopId() != null) {
            productQueryWrapper.eq(ProductEntity::getShopId, queryDTO.getShopId());
        }
        productQueryWrapper.orderByDesc(ProductEntity::getCreateTime);

        Page<ProductEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<ProductEntity> productPage = productRepository.selectPage(pageRequest, productQueryWrapper);
        List<ProductEntity> records = productPage.getRecords();

        Map<Long, ShopEntity> shopMap = buildShopMap(records);
        List<ProductListItemVO> list = new ArrayList<>();
        for (ProductEntity productEntity : records) {
            ProductListItemVO productListItemVO = new ProductListItemVO();
            productListItemVO.setId(productEntity.getId());
            productListItemVO.setProductName(productEntity.getProductName());
            productListItemVO.setProductSubtitle(productEntity.getProductSubtitle());
            productListItemVO.setMainImage(productEntity.getMainImage());
            productListItemVO.setPrice(productEntity.getPrice());
            productListItemVO.setStock(productEntity.getStock());
            productListItemVO.setShop(toShopSimpleVO(shopMap.get(productEntity.getShopId())));
            list.add(productListItemVO);
        }

        PageVO<ProductListItemVO> pageVO = new PageVO<>();
        pageVO.setTotal(productPage.getTotal());
        pageVO.setPageNum(productPage.getCurrent());
        pageVO.setPageSize(productPage.getSize());
        pageVO.setList(list);
        return pageVO;
    }

    /**
     * 商品详情查询（仅允许查看已上架商品）。
     */
    public ProductDetailVO getProductDetail(Long productId) {
        ProductEntity productEntity = productRepository.selectById(productId);
        if (productEntity == null || !Boolean.FALSE.equals(productEntity.getDeleted()) || !"ON_SHELF".equals(productEntity.getSaleStatus())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }

        ShopEntity shopEntity = shopRepository.selectById(productEntity.getShopId());
        ProductDetailVO productDetailVO = new ProductDetailVO();
        productDetailVO.setId(productEntity.getId());
        productDetailVO.setProductSn(productEntity.getProductSn());
        productDetailVO.setProductName(productEntity.getProductName());
        productDetailVO.setProductSubtitle(productEntity.getProductSubtitle());
        productDetailVO.setMainImage(productEntity.getMainImage());
        productDetailVO.setDetail(productEntity.getDetail());
        productDetailVO.setPrice(productEntity.getPrice());
        productDetailVO.setStock(productEntity.getStock());
        productDetailVO.setShop(toShopSimpleVO(shopEntity));
        return productDetailVO;
    }

    private Map<Long, ShopEntity> buildShopMap(List<ProductEntity> productList) {
        List<Long> shopIds = new ArrayList<>();
        for (ProductEntity productEntity : productList) {
            if (productEntity.getShopId() != null) {
                shopIds.add(productEntity.getShopId());
            }
        }
        if (shopIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<ShopEntity> shopQueryWrapper = new LambdaQueryWrapper<>();
        shopQueryWrapper.in(ShopEntity::getId, shopIds);
        List<ShopEntity> shopList = shopRepository.selectList(shopQueryWrapper);
        Map<Long, ShopEntity> shopMap = new HashMap<>();
        for (ShopEntity shopEntity : shopList) {
            shopMap.put(shopEntity.getId(), shopEntity);
        }
        return shopMap;
    }

    private ShopSimpleVO toShopSimpleVO(ShopEntity shopEntity) {
        if (shopEntity == null) {
            return null;
        }
        ShopSimpleVO shopSimpleVO = new ShopSimpleVO();
        shopSimpleVO.setShopId(shopEntity.getId());
        shopSimpleVO.setShopName(shopEntity.getShopName());
        shopSimpleVO.setShopType(shopEntity.getShopType());
        return shopSimpleVO;
    }
}
