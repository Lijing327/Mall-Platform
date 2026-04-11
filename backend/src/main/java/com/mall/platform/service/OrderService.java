package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.OrderCreateDTO;
import com.mall.platform.dto.OrderPayDTO;
import com.mall.platform.config.FeatureFlags;
import com.mall.platform.entity.CartEntity;
import com.mall.platform.entity.OrderEntity;
import com.mall.platform.entity.OrderItemEntity;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.entity.ShopOrderEntity;
import com.mall.platform.entity.UserAddressEntity;
import com.mall.platform.enums.OrderStatus;
import com.mall.platform.enums.ProductSaleStatus;
import com.mall.platform.repository.CartRepository;
import com.mall.platform.repository.OrderItemRepository;
import com.mall.platform.repository.OrderRepository;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.MyOrderItemVO;
import com.mall.platform.vo.MyOrderListVO;
import com.mall.platform.vo.MyOrderShopOrderVO;
import com.mall.platform.vo.OrderConfirmReceiveVO;
import com.mall.platform.vo.OrderCreateVO;
import com.mall.platform.vo.OrderPayVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;
    private final ShopOrderService shopOrderService;
    private final FeatureFlags featureFlags;
    private final UserAddressService userAddressService;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ShopRepository shopRepository,
                        ShopOrderService shopOrderService,
                        FeatureFlags featureFlags,
                        UserAddressService userAddressService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shopRepository = shopRepository;
        this.shopOrderService = shopOrderService;
        this.featureFlags = featureFlags;
        this.userAddressService = userAddressService;
    }

    /**
     * 创建订单：校验库存、扣减库存、写订单主表和订单项快照。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(OrderCreateDTO orderCreateDTO) {
        UserAddressEntity address = userAddressService.findOwnEntity(orderCreateDTO.getAddressId(), orderCreateDTO.getUserId());
        if (address == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "收货地址不存在或不可用");
        }

        LambdaQueryWrapper<CartEntity> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(CartEntity::getUserId, orderCreateDTO.getUserId());
        cartQueryWrapper.eq(CartEntity::getChecked, Boolean.TRUE);
        List<CartEntity> checkedCartList = cartRepository.selectList(cartQueryWrapper);
        if (checkedCartList.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "购物车为空，无法下单");
        }

        List<OrderItemEntity> orderItemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartEntity cartEntity : checkedCartList) {
            ProductEntity productEntity = productRepository.selectById(cartEntity.getProductId());
            if (productEntity == null || Boolean.TRUE.equals(productEntity.getDeleted())
                    || !ProductSaleStatus.ON_SHELF.getCode().equals(productEntity.getSaleStatus())) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "存在无效商品，无法下单");
            }

            Integer remainStock = productEntity.getStock() == null ? 0 : productEntity.getStock();
            if (remainStock < cartEntity.getQuantity()) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品库存不足：" + productEntity.getProductName());
            }

            LambdaUpdateWrapper<ProductEntity> productUpdateWrapper = new LambdaUpdateWrapper<>();
            productUpdateWrapper.eq(ProductEntity::getId, productEntity.getId());
            productUpdateWrapper.ge(ProductEntity::getStock, cartEntity.getQuantity());
            productUpdateWrapper.setSql("stock = stock - " + cartEntity.getQuantity());
            int updateCount = productRepository.update(null, productUpdateWrapper);
            if (updateCount == 0) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品库存扣减失败，请重试");
            }

            BigDecimal itemAmount = productEntity.getPrice().multiply(BigDecimal.valueOf(cartEntity.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);

            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setShopId(productEntity.getShopId());
            orderItemEntity.setProductId(productEntity.getId());
            orderItemEntity.setProductName(productEntity.getProductName());
            orderItemEntity.setProductImage(productEntity.getMainImage());
            orderItemEntity.setProductPrice(productEntity.getPrice());
            orderItemEntity.setQuantity(cartEntity.getQuantity());
            orderItemEntity.setItemAmount(itemAmount);
            orderItemEntity.setItemStatus("NORMAL");
            orderItemList.add(orderItemEntity);
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderNo(generateOrderNo(orderCreateDTO.getUserId()));
        orderEntity.setUserId(orderCreateDTO.getUserId());
        orderEntity.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setPayAmount(totalAmount);
        orderEntity.setReceiverName(address.getReceiverName());
        orderEntity.setReceiverMobile(address.getReceiverMobile());
        orderEntity.setReceiverAddress(buildFullAddress(address));
        if (orderCreateDTO.getRemark() != null && !orderCreateDTO.getRemark().isBlank()) {
            orderEntity.setRemark(orderCreateDTO.getRemark().trim());
        }
        orderRepository.insert(orderEntity);

        for (OrderItemEntity orderItemEntity : orderItemList) {
            orderItemEntity.setOrderId(orderEntity.getId());
            orderItemRepository.insert(orderItemEntity);
        }

        List<Long> cartIds = new ArrayList<>();
        for (CartEntity checkedCart : checkedCartList) {
            cartIds.add(checkedCart.getId());
        }
        LambdaQueryWrapper<CartEntity> removeCartWrapper = new LambdaQueryWrapper<>();
        removeCartWrapper.in(CartEntity::getId, cartIds);
        cartRepository.delete(removeCartWrapper);

        OrderCreateVO orderCreateVO = new OrderCreateVO();
        orderCreateVO.setOrderId(orderEntity.getId());
        orderCreateVO.setOrderNo(orderEntity.getOrderNo());
        orderCreateVO.setOrderStatus(orderEntity.getOrderStatus());
        orderCreateVO.setTotalAmount(orderEntity.getTotalAmount());
        return orderCreateVO;
    }

    /**
     * 模拟支付：仅允许待支付订单支付成功。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderPayVO payOrder(OrderPayDTO orderPayDTO) {
        LambdaQueryWrapper<OrderEntity> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(OrderEntity::getOrderNo, orderPayDTO.getOrderNo());
        orderQueryWrapper.eq(OrderEntity::getUserId, orderPayDTO.getUserId());
        OrderEntity orderEntity = orderRepository.selectOne(orderQueryWrapper);
        if (orderEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(orderEntity.getOrderStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前订单状态不允许支付");
        }

        orderEntity.setOrderStatus(OrderStatus.PAID.getCode());
        orderEntity.setPayType("MOCK");
        orderEntity.setPayTime(LocalDateTime.now());
        orderRepository.updateById(orderEntity);

        OrderPayVO orderPayVO = new OrderPayVO();
        orderPayVO.setOrderNo(orderEntity.getOrderNo());
        orderPayVO.setOrderStatus(orderEntity.getOrderStatus());
        orderPayVO.setPayType(orderEntity.getPayType());
        orderPayVO.setPayTime(orderEntity.getPayTime());
        return orderPayVO;
    }

    /**
     * 用户确认收货：仅允许本人待收货（已发货）订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmReceiveVO confirmReceive(Long orderId, Long userId) {
        OrderEntity orderEntity = orderRepository.selectById(orderId);
        if (orderEntity == null || !userId.equals(orderEntity.getUserId())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (!OrderStatus.SHIPPED.getCode().equals(orderEntity.getOrderStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前订单状态不可确认收货");
        }
        LocalDateTime now = LocalDateTime.now();
        orderEntity.setOrderStatus(OrderStatus.COMPLETED.getCode());
        orderEntity.setCompleteTime(now);
        orderEntity.setUpdateTime(now);
        orderRepository.updateById(orderEntity);

        OrderConfirmReceiveVO vo = new OrderConfirmReceiveVO();
        vo.setOrderId(orderEntity.getId());
        vo.setOrderNo(orderEntity.getOrderNo());
        vo.setOrderStatus(orderEntity.getOrderStatus());
        vo.setCompleteTime(orderEntity.getCompleteTime());
        return vo;
    }

    /**
     * 当前用户的订单列表（含订单项），按创建时间倒序。
     */
    public List<MyOrderListVO> listMyOrders(Long userId) {
        LambdaQueryWrapper<OrderEntity> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(OrderEntity::getUserId, userId);
        orderWrapper.orderByDesc(OrderEntity::getCreateTime);
        List<OrderEntity> orders = orderRepository.selectList(orderWrapper);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = new ArrayList<>();
        for (OrderEntity orderEntity : orders) {
            orderIds.add(orderEntity.getId());
        }

        LambdaQueryWrapper<OrderItemEntity> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItemEntity::getOrderId, orderIds);
        itemWrapper.orderByAsc(OrderItemEntity::getId);
        List<OrderItemEntity> allItems = orderItemRepository.selectList(itemWrapper);

        Map<Long, List<OrderItemEntity>> itemsByOrderId = new HashMap<>();
        for (OrderItemEntity orderItemEntity : allItems) {
            itemsByOrderId.computeIfAbsent(orderItemEntity.getOrderId(), k -> new ArrayList<>()).add(orderItemEntity);
        }

        Set<Long> shopIds = new HashSet<>();
        for (OrderItemEntity orderItemEntity : allItems) {
            shopIds.add(orderItemEntity.getShopId());
        }
        Map<Long, ShopEntity> shopById = new HashMap<>();
        for (Long shopId : shopIds) {
            ShopEntity shopEntity = shopRepository.selectById(shopId);
            if (shopEntity != null) {
                shopById.put(shopId, shopEntity);
            }
        }

        List<ShopOrderEntity> allShopOrders = shopOrderService.listByOrderIds(orderIds);
        Map<Long, List<ShopOrderEntity>> shopOrdersByOrderId = allShopOrders.stream()
                .collect(Collectors.groupingBy(ShopOrderEntity::getOrderId));

        List<MyOrderListVO> result = new ArrayList<>();
        for (OrderEntity orderEntity : orders) {
            MyOrderListVO vo = new MyOrderListVO();
            vo.setOrderId(orderEntity.getId());
            vo.setOrderNo(orderEntity.getOrderNo());
            vo.setOrderStatus(orderEntity.getOrderStatus());
            vo.setTotalAmount(orderEntity.getTotalAmount());
            vo.setPayAmount(orderEntity.getPayAmount());
            vo.setPayType(orderEntity.getPayType());
            vo.setPayTime(orderEntity.getPayTime());
            vo.setCreateTime(orderEntity.getCreateTime());
            vo.setCompleteTime(orderEntity.getCompleteTime());
            vo.setReceiverName(orderEntity.getReceiverName());
            vo.setReceiverMobile(orderEntity.getReceiverMobile());
            vo.setReceiverAddress(orderEntity.getReceiverAddress());
            vo.setRemark(orderEntity.getRemark());
            vo.setShippingNo(orderEntity.getShippingNo());
            vo.setShippingRemark(orderEntity.getShippingRemark());
            vo.setShipTime(orderEntity.getShipTime());

            List<MyOrderItemVO> itemVos = new ArrayList<>();
            for (OrderItemEntity e : itemsByOrderId.getOrDefault(orderEntity.getId(), List.of())) {
                MyOrderItemVO itemVo = new MyOrderItemVO();
                itemVo.setProductId(e.getProductId());
                itemVo.setShopId(e.getShopId());
                itemVo.setProductName(e.getProductName());
                itemVo.setProductImage(e.getProductImage());
                itemVo.setProductPrice(e.getProductPrice());
                itemVo.setQuantity(e.getQuantity());
                itemVo.setItemAmount(e.getItemAmount());
                itemVo.setItemStatus(e.getItemStatus());
                itemVos.add(itemVo);
            }
            vo.setItems(itemVos);

            List<OrderItemEntity> lineItems = itemsByOrderId.getOrDefault(orderEntity.getId(), List.of());
            vo.setShopOrders(buildShopOrdersForDisplay(orderEntity, lineItems, shopById,
                    shopOrdersByOrderId.getOrDefault(orderEntity.getId(), List.of())));

            result.add(vo);
        }
        return result;
    }

    /**
     * 组装子订单展示：优先使用 shop_order 表；否则在关闭拆单开关时返回一条主单派生默认子单；开启拆单且无表数据时按店铺汇总订单行。
     */
    private List<MyOrderShopOrderVO> buildShopOrdersForDisplay(OrderEntity orderEntity,
                                                             List<OrderItemEntity> lineItems,
                                                             Map<Long, ShopEntity> shopById,
                                                             List<ShopOrderEntity> persistedShopOrders) {
        if (!persistedShopOrders.isEmpty()) {
            List<MyOrderShopOrderVO> vos = new ArrayList<>();
            for (ShopOrderEntity e : persistedShopOrders) {
                vos.add(toMyOrderShopOrderVo(e));
            }
            return vos;
        }
        if (featureFlags.splitOrderEnabled()) {
            List<MyOrderShopOrderVO> grouped = buildGroupedSyntheticShopOrders(orderEntity, lineItems, shopById);
            if (!grouped.isEmpty()) {
                return grouped;
            }
            return List.of(buildDefaultSyntheticShopOrder(orderEntity, lineItems, shopById));
        }
        return List.of(buildDefaultSyntheticShopOrder(orderEntity, lineItems, shopById));
    }

    private static MyOrderShopOrderVO toMyOrderShopOrderVo(ShopOrderEntity e) {
        MyOrderShopOrderVO vo = new MyOrderShopOrderVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setShopId(e.getShopId());
        vo.setShopName(e.getShopName());
        vo.setAmount(e.getAmount());
        vo.setStatus(e.getStatus());
        vo.setShippingNo(e.getShippingNo());
        vo.setShippingRemark(e.getShippingRemark());
        vo.setShipTime(e.getShipTime());
        vo.setCompleteTime(e.getCompleteTime());
        return vo;
    }

    /**
     * 单店默认：一条子单，金额与物流字段与主单对齐，店铺取首行订单项所属店铺。
     */
    private static MyOrderShopOrderVO buildDefaultSyntheticShopOrder(OrderEntity orderEntity,
                                                                     List<OrderItemEntity> lineItems,
                                                                     Map<Long, ShopEntity> shopById) {
        MyOrderShopOrderVO vo = new MyOrderShopOrderVO();
        vo.setId(null);
        vo.setOrderId(orderEntity.getId());
        if (lineItems.isEmpty()) {
            vo.setShopId(null);
            vo.setShopName("-");
        } else {
            Long sid = lineItems.get(0).getShopId();
            vo.setShopId(sid);
            ShopEntity shop = shopById.get(sid);
            vo.setShopName(shop != null ? shop.getShopName() : "-");
        }
        vo.setAmount(orderEntity.getPayAmount());
        vo.setStatus(orderEntity.getOrderStatus());
        vo.setShippingNo(orderEntity.getShippingNo());
        vo.setShippingRemark(orderEntity.getShippingRemark());
        vo.setShipTime(orderEntity.getShipTime());
        vo.setCompleteTime(orderEntity.getCompleteTime());
        return vo;
    }

    /**
     * 拆单预览：无 shop_order 表数据时，按店铺汇总金额（物流仍沿用主单字段，仅作占位展示）。
     */
    private static List<MyOrderShopOrderVO> buildGroupedSyntheticShopOrders(OrderEntity orderEntity,
                                                                           List<OrderItemEntity> lineItems,
                                                                           Map<Long, ShopEntity> shopById) {
        Map<Long, BigDecimal> amountByShop = new LinkedHashMap<>();
        for (OrderItemEntity it : lineItems) {
            amountByShop.merge(it.getShopId(), it.getItemAmount(), BigDecimal::add);
        }
        List<MyOrderShopOrderVO> list = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> en : amountByShop.entrySet()) {
            MyOrderShopOrderVO vo = new MyOrderShopOrderVO();
            vo.setId(null);
            vo.setOrderId(orderEntity.getId());
            vo.setShopId(en.getKey());
            ShopEntity shop = shopById.get(en.getKey());
            vo.setShopName(shop != null ? shop.getShopName() : "-");
            vo.setAmount(en.getValue());
            vo.setStatus(orderEntity.getOrderStatus());
            vo.setShippingNo(orderEntity.getShippingNo());
            vo.setShippingRemark(orderEntity.getShippingRemark());
            vo.setShipTime(orderEntity.getShipTime());
            vo.setCompleteTime(orderEntity.getCompleteTime());
            list.add(vo);
        }
        return list;
    }

    private static String buildFullAddress(UserAddressEntity address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) sb.append(address.getProvince());
        if (address.getCity() != null) sb.append(address.getCity());
        if (address.getDistrict() != null) sb.append(address.getDistrict());
        if (address.getDetailAddress() != null) sb.append(address.getDetailAddress());
        return sb.toString();
    }

    private String generateOrderNo(Long userId) {
        String timePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomValue = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return timePrefix + userId + randomValue;
    }
}
