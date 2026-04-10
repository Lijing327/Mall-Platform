package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.dto.MerchantAuditDTO;
import com.mall.platform.service.MerchantService;
import com.mall.platform.vo.MerchantAuditVO;
import com.mall.platform.vo.MerchantListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    private final MerchantService merchantService;

    public AdminMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * 管理员查询商家申请列表。
     */
    @GetMapping("/list")
    public Result<List<MerchantListItemVO>> list() {
        return Result.success(merchantService.listForAdmin());
    }

    /**
     * 管理员审核商家申请。
     */
    @PostMapping("/audit")
    public Result<MerchantAuditVO> audit(@Valid @RequestBody MerchantAuditDTO merchantAuditDTO) {
        return Result.success(merchantService.audit(merchantAuditDTO));
    }
}
