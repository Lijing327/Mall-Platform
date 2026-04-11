package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.MerchantApplyDTO;
import com.mall.platform.dto.MerchantAuditDTO;
import com.mall.platform.entity.MerchantEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.enums.MerchantApplyStatus;
import com.mall.platform.repository.MerchantRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.MerchantApplyVO;
import com.mall.platform.vo.MerchantAuditVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;

    public MerchantService(MerchantRepository merchantRepository, ShopRepository shopRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 提交商家入驻申请（MVP: 同一用户仅保留一条申请记录）。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplyVO apply(MerchantApplyDTO merchantApplyDTO) {
        LambdaQueryWrapper<MerchantEntity> merchantQueryWrapper = new LambdaQueryWrapper<>();
        merchantQueryWrapper.eq(MerchantEntity::getUserId, merchantApplyDTO.getUserId());
        MerchantEntity existed = merchantRepository.selectOne(merchantQueryWrapper);
        if (existed != null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "该用户已提交过商家申请");
        }

        MerchantEntity merchantEntity = new MerchantEntity();
        merchantEntity.setUserId(merchantApplyDTO.getUserId());
        merchantEntity.setMerchantCode(generateMerchantCode());
        merchantEntity.setMerchantName(merchantApplyDTO.getMerchantName());
        merchantEntity.setContactName(merchantApplyDTO.getContactName());
        merchantEntity.setContactMobile(merchantApplyDTO.getContactMobile());
        merchantEntity.setApplyStatus(MerchantApplyStatus.PENDING.getCode());
        merchantEntity.setApplyTime(LocalDateTime.now());
        merchantEntity.setStatus("ENABLED");

        String qualificationText = merchantApplyDTO.getQualificationText();
        if (StringUtils.hasText(qualificationText)) {
            merchantEntity.setAuditRemark("申请资料: " + qualificationText.trim());
        }

        merchantRepository.insert(merchantEntity);

        MerchantApplyVO merchantApplyVO = new MerchantApplyVO();
        merchantApplyVO.setMerchantId(merchantEntity.getId());
        merchantApplyVO.setMerchantCode(merchantEntity.getMerchantCode());
        merchantApplyVO.setApplyStatus(merchantEntity.getApplyStatus());
        merchantApplyVO.setApplyTime(merchantEntity.getApplyTime());
        return merchantApplyVO;
    }

    /**
     * 管理员审核：通过时自动创建第三方商家店铺。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantAuditVO audit(MerchantAuditDTO merchantAuditDTO) {
        MerchantEntity merchantEntity = merchantRepository.selectById(merchantAuditDTO.getMerchantId());
        if (merchantEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商家申请不存在");
        }
        if (!MerchantApplyStatus.PENDING.getCode().equals(merchantEntity.getApplyStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前申请不是待审核状态");
        }

        LocalDateTime auditTime = LocalDateTime.now();
        merchantEntity.setAuditTime(auditTime);
        merchantEntity.setAuditRemark(merchantAuditDTO.getAuditRemark());

        Long shopId = null;
        String action = merchantAuditDTO.getAuditAction().trim().toUpperCase();
        if ("APPROVE".equals(action)) {
            merchantEntity.setApplyStatus(MerchantApplyStatus.APPROVED.getCode());

            // MVP 按一个商家一个店铺实现，若已存在则不重复创建
            LambdaQueryWrapper<ShopEntity> shopQueryWrapper = new LambdaQueryWrapper<>();
            shopQueryWrapper.eq(ShopEntity::getMerchantId, merchantEntity.getId());
            ShopEntity existedShop = shopRepository.selectOne(shopQueryWrapper);
            if (existedShop == null) {
                ShopEntity shopEntity = new ShopEntity();
                shopEntity.setShopCode(generateShopCode());
                shopEntity.setShopName(merchantEntity.getMerchantName() + "店铺");
                shopEntity.setShopType("MERCHANT");
                shopEntity.setMerchantId(merchantEntity.getId());
                shopEntity.setOwnerUserId(merchantEntity.getUserId());
                shopEntity.setStatus("ENABLED");
                shopRepository.insert(shopEntity);
                shopId = shopEntity.getId();
            } else {
                shopId = existedShop.getId();
            }
        } else if ("REJECT".equals(action)) {
            merchantEntity.setApplyStatus(MerchantApplyStatus.REJECTED.getCode());
            if (!StringUtils.hasText(merchantAuditDTO.getAuditRemark())) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "驳回时请填写驳回原因");
            }
        } else {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "auditAction 仅支持 APPROVE 或 REJECT");
        }

        merchantRepository.updateById(merchantEntity);

        MerchantAuditVO merchantAuditVO = new MerchantAuditVO();
        merchantAuditVO.setMerchantId(merchantEntity.getId());
        merchantAuditVO.setApplyStatus(merchantEntity.getApplyStatus());
        merchantAuditVO.setAuditRemark(merchantEntity.getAuditRemark());
        merchantAuditVO.setAuditTime(merchantEntity.getAuditTime());
        merchantAuditVO.setShopId(shopId);
        return merchantAuditVO;
    }

    private String generateMerchantCode() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "M" + timePart + randomPart;
    }

    private String generateShopCode() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "S" + timePart + randomPart;
    }
}
