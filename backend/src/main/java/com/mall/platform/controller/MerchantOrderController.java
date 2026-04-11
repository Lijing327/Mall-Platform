package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.MerchantOrderQueryDTO;
import com.mall.platform.dto.MerchantShipDTO;
import com.mall.platform.service.MerchantOrderService;
import com.mall.platform.vo.MerchantOrderDetailVO;
import com.mall.platform.vo.MerchantOrderListVO;
import com.mall.platform.vo.MerchantShipVO;
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

@Validated
@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    /**
     * 商家订单分页列表。
     */
    @GetMapping
    public Result<PageVO<MerchantOrderListVO>> list(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize) {
        MerchantOrderQueryDTO queryDTO = new MerchantOrderQueryDTO();
        queryDTO.setUserId(AuthBinding.currentUserIdOrThrow());
        queryDTO.setMerchantId(merchantId);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        return Result.success(merchantOrderService.listOrders(queryDTO));
    }

    /**
     * 商家订单详情。
     */
    @GetMapping("/{id}")
    public Result<MerchantOrderDetailVO> detail(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Long merchantId) {
        return Result.success(merchantOrderService.getOrderDetail(id, AuthBinding.currentUserIdOrThrow(), merchantId));
    }

    /**
     * 商家订单发货。
     */
    @PostMapping("/{id}/ship")
    public Result<MerchantShipVO> ship(@PathVariable("id") Long id, @Valid @RequestBody MerchantShipDTO shipDTO) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(shipDTO.getUserId(), uid);
        shipDTO.setUserId(uid);
        return Result.success(merchantOrderService.shipOrder(id, shipDTO));
    }
}
