package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.OrderCreateDTO;
import com.mall.platform.dto.OrderPayDTO;
import com.mall.platform.service.OrderService;
import com.mall.platform.vo.OrderCreateVO;
import com.mall.platform.vo.OrderPayVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 基于购物车创建订单。
     */
    @PostMapping("/create")
    public Result<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        return Result.success(orderService.createOrder(orderCreateDTO));
    }

    /**
     * 模拟支付接口。
     */
    @PostMapping("/pay")
    public Result<OrderPayVO> pay(@Valid @RequestBody OrderPayDTO orderPayDTO) {
        return Result.success(orderService.payOrder(orderPayDTO));
    }
}
