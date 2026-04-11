package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.MerchantShopShipDTO;
import com.mall.platform.service.MerchantShopOrderService;
import com.mall.platform.vo.MyOrderShopOrderVO;
import com.mall.platform.vo.PageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家子订单（shop_order）相关接口。
 * 列表：已接库查询；发货：预留，请继续使用 {@code /api/merchant/orders/{id}/ship}。
 */
@Validated
@RestController
@RequestMapping("/api/merchant/shop-orders")
public class MerchantShopOrderController {

    private final MerchantShopOrderService merchantShopOrderService;

    public MerchantShopOrderController(MerchantShopOrderService merchantShopOrderService) {
        this.merchantShopOrderService = merchantShopOrderService;
    }

    /**
     * 子订单分页列表（按当前商家店铺 shop_id 过滤）。
     */
    @GetMapping
    public Result<PageVO<MyOrderShopOrderVO>> list(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize) {
        return Result.success(merchantShopOrderService.listShopOrders(
                AuthBinding.currentUserIdOrThrow(), merchantId, pageNum, pageSize));
    }

    /**
     * 子订单发货（预留占位，调用将返回业务错误说明，不修改数据）。
     */
    @PostMapping("/{id}/ship")
    public Result<String> ship(@PathVariable("id") Long id, @Valid @RequestBody MerchantShopShipDTO shipDTO) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(shipDTO.getUserId(), uid);
        shipDTO.setUserId(uid);
        merchantShopOrderService.shipShopOrder(id, shipDTO);
        throw new IllegalStateException("unreachable");
    }
}
