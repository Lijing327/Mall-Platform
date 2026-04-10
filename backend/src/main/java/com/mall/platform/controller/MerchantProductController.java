package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.MerchantProductCreateDTO;
import com.mall.platform.dto.MerchantProductQueryDTO;
import com.mall.platform.dto.MerchantProductUpdateDTO;
import com.mall.platform.service.MerchantProductService;
import com.mall.platform.vo.MerchantProductVO;
import com.mall.platform.vo.PageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    public MerchantProductController(MerchantProductService merchantProductService) {
        this.merchantProductService = merchantProductService;
    }

    /**
     * 商家商品分页列表。
     */
    @GetMapping
    public Result<PageVO<MerchantProductVO>> list(
            @RequestParam @NotNull(message = "userId 不能为空") Long userId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        MerchantProductQueryDTO queryDTO = new MerchantProductQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setMerchantId(merchantId);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setKeyword(keyword);
        return Result.success(merchantProductService.listProducts(queryDTO));
    }

    /**
     * 商家新增商品。
     */
    @PostMapping
    public Result<MerchantProductVO> create(@Valid @RequestBody MerchantProductCreateDTO createDTO) {
        return Result.success(merchantProductService.createProduct(createDTO));
    }

    /**
     * 商家修改商品。
     */
    @PutMapping("/{id}")
    public Result<MerchantProductVO> update(@PathVariable("id") Long id, @Valid @RequestBody MerchantProductUpdateDTO updateDTO) {
        return Result.success(merchantProductService.updateProduct(id, updateDTO));
    }

    /**
     * 商家商品上架。
     */
    @PostMapping("/{id}/on-shelf")
    public Result<String> onShelf(
            @PathVariable("id") Long id,
            @RequestParam @NotNull(message = "userId 不能为空") Long userId,
            @RequestParam(required = false) Long merchantId) {
        merchantProductService.onShelf(id, userId, merchantId);
        return Result.success("上架成功", "OK");
    }

    /**
     * 商家商品下架。
     */
    @PostMapping("/{id}/off-shelf")
    public Result<String> offShelf(
            @PathVariable("id") Long id,
            @RequestParam @NotNull(message = "userId 不能为空") Long userId,
            @RequestParam(required = false) Long merchantId) {
        merchantProductService.offShelf(id, userId, merchantId);
        return Result.success("下架成功", "OK");
    }

    /**
     * 商家删除商品（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable("id") Long id,
            @RequestParam @NotNull(message = "userId 不能为空") Long userId,
            @RequestParam(required = false) Long merchantId) {
        merchantProductService.deleteProduct(id, userId, merchantId);
        return Result.success("删除成功", "OK");
    }
}
