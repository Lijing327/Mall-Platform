package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.OrderCreateDTO;
import com.mall.platform.dto.OrderPayDTO;
import com.mall.platform.service.OrderService;
import com.mall.platform.vo.MyOrderListVO;
import com.mall.platform.vo.OrderConfirmReceiveVO;
import com.mall.platform.vo.OrderCreateVO;
import com.mall.platform.vo.OrderPayVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 我的订单列表（含订单项）。
     */
    @GetMapping("/my")
    public Result<List<MyOrderListVO>> myOrders() {
        return Result.success(orderService.listMyOrders(AuthBinding.currentUserIdOrThrow()));
    }

    /**
     * 基于购物车创建订单。
     */
    @PostMapping("/create")
    public Result<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(orderCreateDTO.getUserId(), uid);
        orderCreateDTO.setUserId(uid);
        return Result.success(orderService.createOrder(orderCreateDTO));
    }

    /**
     * 模拟支付接口。
     */
    @PostMapping("/pay")
    public Result<OrderPayVO> pay(@Valid @RequestBody OrderPayDTO orderPayDTO) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(orderPayDTO.getUserId(), uid);
        orderPayDTO.setUserId(uid);
        return Result.success(orderService.payOrder(orderPayDTO));
    }

    /**
     * 确认收货（已发货 -> 已完成）。
     */
    @PostMapping("/{id}/confirm-receive")
    public Result<OrderConfirmReceiveVO> confirmReceive(@PathVariable("id") Long id) {
        return Result.success(orderService.confirmReceive(id, AuthBinding.currentUserIdOrThrow()));
    }
}
