package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.CartAddDTO;
import com.mall.platform.entity.CartEntity;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.repository.CartRepository;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.CartItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, ShopRepository shopRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 加入购物车：同用户同商品存在时累加数量。
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(CartAddDTO cartAddDTO) {
        ProductEntity productEntity = productRepository.selectById(cartAddDTO.getProductId());
        if (productEntity == null || Boolean.TRUE.equals(productEntity.getDeleted()) || !"ON_SHELF".equals(productEntity.getSaleStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品不存在或未上架");
        }

        LambdaQueryWrapper<CartEntity> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(CartEntity::getUserId, cartAddDTO.getUserId());
        cartQueryWrapper.eq(CartEntity::getProductId, cartAddDTO.getProductId());
        CartEntity existed = cartRepository.selectOne(cartQueryWrapper);
        if (existed == null) {
            CartEntity cartEntity = new CartEntity();
            cartEntity.setUserId(cartAddDTO.getUserId());
            cartEntity.setProductId(cartAddDTO.getProductId());
            cartEntity.setShopId(productEntity.getShopId());
            cartEntity.setQuantity(cartAddDTO.getQuantity());
            cartEntity.setChecked(Boolean.TRUE);
            cartRepository.insert(cartEntity);
            return;
        }

        existed.setQuantity(existed.getQuantity() + cartAddDTO.getQuantity());
        cartRepository.updateById(existed);
    }

    /**
     * 查询用户购物车。
     */
    public List<CartItemVO> listCart(Long userId) {
        LambdaQueryWrapper<CartEntity> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(CartEntity::getUserId, userId);
        cartQueryWrapper.orderByDesc(CartEntity::getCreateTime);
        List<CartEntity> cartList = cartRepository.selectList(cartQueryWrapper);
        if (cartList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> productIds = new ArrayList<>();
        List<Long> shopIds = new ArrayList<>();
        for (CartEntity cartEntity : cartList) {
            productIds.add(cartEntity.getProductId());
            shopIds.add(cartEntity.getShopId());
        }
        Map<Long, ProductEntity> productMap = buildProductMap(productIds);
        Map<Long, ShopEntity> shopMap = buildShopMap(shopIds);

        List<CartItemVO> result = new ArrayList<>();
        for (CartEntity cartEntity : cartList) {
            ProductEntity productEntity = productMap.get(cartEntity.getProductId());
            ShopEntity shopEntity = shopMap.get(cartEntity.getShopId());
            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setCartId(cartEntity.getId());
            cartItemVO.setProductId(cartEntity.getProductId());
            if (productEntity != null) {
                cartItemVO.setProductName(productEntity.getProductName());
                cartItemVO.setProductImage(productEntity.getMainImage());
                cartItemVO.setProductPrice(productEntity.getPrice());
            }
            cartItemVO.setQuantity(cartEntity.getQuantity());
            cartItemVO.setShopId(cartEntity.getShopId());
            if (shopEntity != null) {
                cartItemVO.setShopName(shopEntity.getShopName());
                cartItemVO.setShopType(shopEntity.getShopType());
            }
            result.add(cartItemVO);
        }
        return result;
    }

    private Map<Long, ProductEntity> buildProductMap(List<Long> productIds) {
        LambdaQueryWrapper<ProductEntity> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.in(ProductEntity::getId, productIds);
        List<ProductEntity> productList = productRepository.selectList(productQueryWrapper);
        Map<Long, ProductEntity> productMap = new HashMap<>();
        for (ProductEntity productEntity : productList) {
            productMap.put(productEntity.getId(), productEntity);
        }
        return productMap;
    }

    private Map<Long, ShopEntity> buildShopMap(List<Long> shopIds) {
        LambdaQueryWrapper<ShopEntity> shopQueryWrapper = new LambdaQueryWrapper<>();
        shopQueryWrapper.in(ShopEntity::getId, shopIds);
        List<ShopEntity> shopList = shopRepository.selectList(shopQueryWrapper);
        Map<Long, ShopEntity> shopMap = new HashMap<>();
        for (ShopEntity shopEntity : shopList) {
            shopMap.put(shopEntity.getId(), shopEntity);
        }
        return shopMap;
    }
}
