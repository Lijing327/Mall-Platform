package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.ProductQueryDTO;
import com.mall.platform.service.ProductService;
import com.mall.platform.vo.PageVO;
import com.mall.platform.vo.ProductDetailVO;
import com.mall.platform.vo.ProductListItemVO;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品分页列表接口：支持名称模糊搜索与按店铺筛选。
     */
    @GetMapping
    public Result<PageVO<ProductListItemVO>> listProducts(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shopId) {
        ProductQueryDTO productQueryDTO = new ProductQueryDTO();
        productQueryDTO.setPageNum(pageNum);
        productQueryDTO.setPageSize(pageSize);
        productQueryDTO.setKeyword(keyword);
        productQueryDTO.setShopId(shopId);
        return Result.success(productService.listProducts(productQueryDTO));
    }

    /**
     * 商品详情接口：仅返回已上架商品。
     */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable("id") Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
