package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.OrderCreateDTO;
import com.mall.platform.dto.OrderPayDTO;
import com.mall.platform.entity.CartEntity;
import com.mall.platform.entity.OrderEntity;
import com.mall.platform.entity.OrderItemEntity;
import com.mall.platform.entity.ProductEntity;
import com.mall.platform.repository.CartRepository;
import com.mall.platform.repository.OrderItemRepository;
import com.mall.platform.repository.OrderRepository;
import com.mall.platform.repository.ProductRepository;
import com.mall.platform.vo.OrderCreateVO;
import com.mall.platform.vo.OrderPayVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final String ORDER_STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String ORDER_STATUS_PAID = "PAID";

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 创建订单：校验库存、扣减库存、写订单主表和订单项快照。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(OrderCreateDTO orderCreateDTO) {
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
            if (productEntity == null || Boolean.TRUE.equals(productEntity.getDeleted()) || !"ON_SHELF".equals(productEntity.getSaleStatus())) {
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
        orderEntity.setOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setPayAmount(totalAmount);
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
        if (!ORDER_STATUS_PENDING_PAYMENT.equals(orderEntity.getOrderStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前订单状态不允许支付");
        }

        orderEntity.setOrderStatus(ORDER_STATUS_PAID);
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

    private String generateOrderNo(Long userId) {
        String timePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomValue = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return timePrefix + userId + randomValue;
    }
}
