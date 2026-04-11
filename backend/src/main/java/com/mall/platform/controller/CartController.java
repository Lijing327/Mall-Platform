package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.CartAddDTO;
import com.mall.platform.service.CartService;
import com.mall.platform.vo.CartItemVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(cartAddDTO.getUserId(), uid);
        cartAddDTO.setUserId(uid);
        cartService.addToCart(cartAddDTO);
        return Result.success("加入购物车成功", "OK");
    }

    /**
     * 查询购物车列表接口。
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.listCart(AuthBinding.currentUserIdOrThrow()));
    }
}
