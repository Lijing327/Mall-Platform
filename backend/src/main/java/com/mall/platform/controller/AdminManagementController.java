package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.AdminMerchantQueryDTO;
import com.mall.platform.dto.AdminOrderQueryDTO;
import com.mall.platform.dto.AdminProductQueryDTO;
import com.mall.platform.dto.MerchantAuditDTO;
import com.mall.platform.service.AdminService;
import com.mall.platform.service.MerchantService;
import com.mall.platform.vo.AdminMerchantVO;
import com.mall.platform.vo.AdminOrderVO;
import com.mall.platform.vo.AdminProductVO;
import com.mall.platform.vo.MerchantAuditVO;
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
@RequestMapping("/api/admin")
public class AdminManagementController {

    private final AdminService adminService;
    private final MerchantService merchantService;

    public AdminManagementController(AdminService adminService, MerchantService merchantService) {
        this.adminService = adminService;
        this.merchantService = merchantService;
    }

    /**
     * 管理员商家申请列表。
     */
    @GetMapping("/merchants")
    public Result<PageVO<AdminMerchantVO>> merchants(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize,
            @RequestParam(required = false) String applyStatus,
            @RequestParam(required = false) String keyword) {
        AdminMerchantQueryDTO queryDTO = new AdminMerchantQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setApplyStatus(applyStatus);
        queryDTO.setKeyword(keyword);
        return Result.success(adminService.listMerchants(queryDTO));
    }

    /**
     * 管理员审核商家申请。
     */
    @PostMapping("/merchants/audit")
    public Result<MerchantAuditVO> auditMerchant(@Valid @RequestBody MerchantAuditDTO merchantAuditDTO) {
        return Result.success(merchantService.audit(merchantAuditDTO));
    }

    /**
     * 管理员订单列表。
     */
    @GetMapping("/orders")
    public Result<PageVO<AdminOrderVO>> orders(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String orderNo) {
        AdminOrderQueryDTO queryDTO = new AdminOrderQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setOrderStatus(orderStatus);
        queryDTO.setOrderNo(orderNo);
        return Result.success(adminService.listOrders(queryDTO));
    }

    /**
     * 管理员商品列表。
     */
    @GetMapping("/products")
    public Result<PageVO<AdminProductVO>> products(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须大于 0") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String saleStatus) {
        AdminProductQueryDTO queryDTO = new AdminProductQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setKeyword(keyword);
        queryDTO.setShopId(shopId);
        queryDTO.setSaleStatus(saleStatus);
        return Result.success(adminService.listProducts(queryDTO));
    }

    /**
     * 管理员下架商品（预留能力）。
     */
    @PostMapping("/products/{id}/off-shelf")
    public Result<String> offShelfProduct(@PathVariable("id") Long id) {
        adminService.offShelfProduct(id);
        return Result.success("下架成功", "OK");
    }
}
