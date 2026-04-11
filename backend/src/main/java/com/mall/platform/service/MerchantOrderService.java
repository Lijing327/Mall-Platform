package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.MerchantOrderQueryDTO;
import com.mall.platform.dto.MerchantShipDTO;
import com.mall.platform.entity.MerchantEntity;
import com.mall.platform.entity.OrderEntity;
import com.mall.platform.entity.OrderItemEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.enums.MerchantApplyStatus;
import com.mall.platform.enums.OrderStatus;
import com.mall.platform.repository.MerchantRepository;
import com.mall.platform.repository.OrderItemRepository;
import com.mall.platform.repository.OrderRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.MerchantOrderDetailVO;
import com.mall.platform.vo.MerchantOrderItemVO;
import com.mall.platform.vo.MerchantOrderListVO;
import com.mall.platform.vo.MerchantShipVO;
import com.mall.platform.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MerchantOrderService {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public MerchantOrderService(MerchantRepository merchantRepository,
                                ShopRepository shopRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 商家查看自己店铺相关订单（按订单项 shop_id 过滤）。
     */
    public PageVO<MerchantOrderListVO> listOrders(MerchantOrderQueryDTO queryDTO) {
        ShopEntity shopEntity = resolveMerchantShop(queryDTO.getUserId(), queryDTO.getMerchantId());
        Integer pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum();
        Integer pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 || queryDTO.getPageSize() > 100 ? 10 : queryDTO.getPageSize();

        LambdaQueryWrapper<OrderItemEntity> orderItemQueryWrapper = new LambdaQueryWrapper<>();
        orderItemQueryWrapper.eq(OrderItemEntity::getShopId, shopEntity.getId());
        orderItemQueryWrapper.orderByDesc(OrderItemEntity::getCreateTime);
        List<OrderItemEntity> orderItemList = orderItemRepository.selectList(orderItemQueryWrapper);
        if (orderItemList.isEmpty()) {
            PageVO<MerchantOrderListVO> emptyPage = new PageVO<>();
            emptyPage.setTotal(0L);
            emptyPage.setPageNum((long) pageNum);
            emptyPage.setPageSize((long) pageSize);
            emptyPage.setList(new ArrayList<>());
            return emptyPage;
        }

        Set<Long> orderIdSet = new LinkedHashSet<>();
        for (OrderItemEntity orderItemEntity : orderItemList) {
            orderIdSet.add(orderItemEntity.getOrderId());
        }

        LambdaQueryWrapper<OrderEntity> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.in(OrderEntity::getId, orderIdSet);
        orderQueryWrapper.orderByDesc(OrderEntity::getCreateTime);
        Page<OrderEntity> pageRequest = new Page<>(pageNum, pageSize);
        IPage<OrderEntity> page = orderRepository.selectPage(pageRequest, orderQueryWrapper);

        List<MerchantOrderListVO> list = new ArrayList<>();
        for (OrderEntity orderEntity : page.getRecords()) {
            MerchantOrderListVO merchantOrderListVO = new MerchantOrderListVO();
            merchantOrderListVO.setOrderId(orderEntity.getId());
            merchantOrderListVO.setOrderNo(orderEntity.getOrderNo());
            merchantOrderListVO.setOrderStatus(orderEntity.getOrderStatus());
            merchantOrderListVO.setPayAmount(orderEntity.getPayAmount());
            merchantOrderListVO.setPayType(orderEntity.getPayType());
            merchantOrderListVO.setPayTime(orderEntity.getPayTime());
            merchantOrderListVO.setCreateTime(orderEntity.getCreateTime());
            merchantOrderListVO.setShippingNo(orderEntity.getShippingNo());
            merchantOrderListVO.setShipTime(orderEntity.getShipTime());
            list.add(merchantOrderListVO);
        }

        PageVO<MerchantOrderListVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(page.getCurrent());
        pageVO.setPageSize(page.getSize());
        pageVO.setList(list);
        return pageVO;
    }

    /**
     * 商家查看订单详情（仅本店商品项）。
     */
    public MerchantOrderDetailVO getOrderDetail(Long orderId, Long userId, Long merchantId) {
        ShopEntity shopEntity = resolveMerchantShop(userId, merchantId);
        List<OrderItemEntity> merchantItems = getMerchantOrderItems(orderId, shopEntity.getId());
        if (merchantItems.isEmpty()) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在或不属于当前商家");
        }

        OrderEntity orderEntity = orderRepository.selectById(orderId);
        if (orderEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }

        MerchantOrderDetailVO detailVO = new MerchantOrderDetailVO();
        detailVO.setOrderId(orderEntity.getId());
        detailVO.setOrderNo(orderEntity.getOrderNo());
        detailVO.setOrderStatus(orderEntity.getOrderStatus());
        detailVO.setTotalAmount(orderEntity.getTotalAmount());
        detailVO.setPayAmount(orderEntity.getPayAmount());
        detailVO.setPayType(orderEntity.getPayType());
        detailVO.setPayTime(orderEntity.getPayTime());
        detailVO.setReceiverName(orderEntity.getReceiverName());
        detailVO.setReceiverMobile(orderEntity.getReceiverMobile());
        detailVO.setReceiverAddress(orderEntity.getReceiverAddress());
        detailVO.setRemark(orderEntity.getRemark());
        detailVO.setShippingNo(orderEntity.getShippingNo());
        detailVO.setShippingRemark(orderEntity.getShippingRemark());
        detailVO.setShipTime(orderEntity.getShipTime());

        List<MerchantOrderItemVO> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : merchantItems) {
            MerchantOrderItemVO itemVO = new MerchantOrderItemVO();
            itemVO.setOrderItemId(itemEntity.getId());
            itemVO.setProductId(itemEntity.getProductId());
            itemVO.setProductName(itemEntity.getProductName());
            itemVO.setProductImage(itemEntity.getProductImage());
            itemVO.setProductPrice(itemEntity.getProductPrice());
            itemVO.setQuantity(itemEntity.getQuantity());
            itemVO.setItemAmount(itemEntity.getItemAmount());
            items.add(itemVO);
        }
        detailVO.setItems(items);
        return detailVO;
    }

    /**
     * 商家发货：仅允许已支付订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantShipVO shipOrder(Long orderId, MerchantShipDTO shipDTO) {
        ShopEntity shopEntity = resolveMerchantShop(shipDTO.getUserId(), shipDTO.getMerchantId());
        List<OrderItemEntity> merchantItems = getMerchantOrderItems(orderId, shopEntity.getId());
        if (merchantItems.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "无权操作不属于本店的订单");
        }

        OrderEntity orderEntity = orderRepository.selectById(orderId);
        if (orderEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (!OrderStatus.PAID.getCode().equals(orderEntity.getOrderStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅已支付订单可发货");
        }

        LocalDateTime now = LocalDateTime.now();
        orderEntity.setOrderStatus(OrderStatus.SHIPPED.getCode());
        orderEntity.setShippingNo(shipDTO.getShippingNo());
        String shipRm = shipDTO.getShippingRemark();
        orderEntity.setShippingRemark(
                shipRm == null || shipRm.trim().isEmpty() ? null : shipRm.trim());
        orderEntity.setShipTime(now);
        orderEntity.setUpdateTime(now);
        orderRepository.updateById(orderEntity);

        MerchantShipVO shipVO = new MerchantShipVO();
        shipVO.setOrderId(orderEntity.getId());
        shipVO.setOrderNo(orderEntity.getOrderNo());
        shipVO.setOrderStatus(orderEntity.getOrderStatus());
        shipVO.setShippingNo(orderEntity.getShippingNo());
        shipVO.setShippingRemark(orderEntity.getShippingRemark());
        shipVO.setShipTime(orderEntity.getShipTime());
        return shipVO;
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
        if (merchantEntity == null || !MerchantApplyStatus.APPROVED.getCode().equals(merchantEntity.getApplyStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前用户不是已审核通过商家");
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

    private List<OrderItemEntity> getMerchantOrderItems(Long orderId, Long shopId) {
        LambdaQueryWrapper<OrderItemEntity> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderItemEntity::getOrderId, orderId);
        itemQueryWrapper.eq(OrderItemEntity::getShopId, shopId);
        return orderItemRepository.selectList(itemQueryWrapper);
    }
}
