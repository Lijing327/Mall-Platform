package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.CartAddDTO;
import com.mall.platform.service.CartService;
import com.mall.platform.vo.CartItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 加入购物车接口。
     */
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody CartAddDTO cartAddDTO) {
        cartService.addToCart(cartAddDTO);
        return Result.success("加入购物车成功", "OK");
    }

    /**
     * 查询购物车列表接口。
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list(@RequestParam @NotNull(message = "userId 不能为空") Long userId) {
        return Result.success(cartService.listCart(userId));
    }
}
