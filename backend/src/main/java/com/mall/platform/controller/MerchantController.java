package com.mall.platform.controller;

import com.mall.platform.auth.AuthBinding;
import com.mall.platform.common.Result;
import com.mall.platform.dto.MerchantApplyDTO;
import com.mall.platform.service.MerchantService;
import com.mall.platform.vo.MerchantApplyVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * 用户提交商家入驻申请。
     */
    @PostMapping("/apply")
    public Result<MerchantApplyVO> apply(@Valid @RequestBody MerchantApplyDTO merchantApplyDTO) {
        long uid = AuthBinding.currentUserIdOrThrow();
        AuthBinding.assertSameUser(merchantApplyDTO.getUserId(), uid);
        merchantApplyDTO.setUserId(uid);
        return Result.success(merchantService.apply(merchantApplyDTO));
    }
}
