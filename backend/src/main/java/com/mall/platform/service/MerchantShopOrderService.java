package com.mall.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.dto.MerchantShopShipDTO;
import com.mall.platform.entity.MerchantEntity;
import com.mall.platform.entity.ShopEntity;
import com.mall.platform.entity.ShopOrderEntity;
import com.mall.platform.enums.MerchantApplyStatus;
import com.mall.platform.repository.MerchantRepository;
import com.mall.platform.repository.ShopOrderRepository;
import com.mall.platform.repository.ShopRepository;
import com.mall.platform.vo.MyOrderShopOrderVO;
import com.mall.platform.vo.PageVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商家侧子订单（shop_order）查询与发货入口骨架。
 * 说明：列表接口已可查库；发货接口为预留，不改变现有主订单发货逻辑。
 */
@Service
public class MerchantShopOrderService {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;
    private final ShopOrderRepository shopOrderRepository;

    public MerchantShopOrderService(MerchantRepository merchantRepository,
                                    ShopRepository shopRepository,
                                    ShopOrderRepository shopOrderRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
        this.shopOrderRepository = shopOrderRepository;
    }

    /**
     * 分页查询当前商家店铺下的子订单。主流程未写入 shop_order 时通常为空列表。
     */
    public PageVO<MyOrderShopOrderVO> listShopOrders(Long userId, Long merchantId, Integer pageNum, Integer pageSize) {
        ShopEntity shopEntity = resolveMerchantShop(userId, merchantId);
        int pn = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int ps = pageSize == null || pageSize < 1 || pageSize > 100 ? 10 : pageSize;

        LambdaQueryWrapper<ShopOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrderEntity::getShopId, shopEntity.getId());
        wrapper.orderByDesc(ShopOrderEntity::getCreateTime);

        Page<ShopOrderEntity> pageRequest = new Page<>(pn, ps);
        IPage<ShopOrderEntity> page = shopOrderRepository.selectPage(pageRequest, wrapper);

        List<MyOrderShopOrderVO> rows = new ArrayList<>();
        for (ShopOrderEntity e : page.getRecords()) {
            rows.add(toRow(e));
        }

        PageVO<MyOrderShopOrderVO> pageVO = new PageVO<>();
        pageVO.setTotal(page.getTotal());
        pageVO.setPageNum(page.getCurrent());
        pageVO.setPageSize(page.getSize());
        pageVO.setList(rows);
        return pageVO;
    }

    /**
     * 子订单发货（预留）：当前不更新任何表，调用方应继续使用主订单发货接口。
     */
    public void shipShopOrder(Long shopOrderId, MerchantShopShipDTO shipDTO) {
        resolveMerchantShop(shipDTO.getUserId(), shipDTO.getMerchantId());
        throw new BizException(ResultCode.BAD_REQUEST.getCode(),
                "子订单发货为预留能力，尚未接入（shopOrderId=" + shopOrderId
                        + "）；请使用主订单接口：POST /api/merchant/orders/{订单ID}/ship");
    }

    private static MyOrderShopOrderVO toRow(ShopOrderEntity e) {
        MyOrderShopOrderVO vo = new MyOrderShopOrderVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setShopId(e.getShopId());
        vo.setShopName(e.getShopName());
        vo.setAmount(e.getAmount());
        vo.setStatus(e.getStatus());
        vo.setShippingNo(e.getShippingNo());
        vo.setShippingRemark(e.getShippingRemark());
        vo.setShipTime(e.getShipTime());
        vo.setCompleteTime(e.getCompleteTime());
        return vo;
    }

    private ShopEntity resolveMerchantShop(Long userId, Long merchantId) {
        if (userId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "userId 不能为空");
        }
        LambdaQueryWrapper<MerchantEntity> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(MerchantEntity::getUserId, userId);
        if (merchantId != null) {
            merchantWrapper.eq(MerchantEntity::getId, merchantId);
        }
        MerchantEntity merchantEntity = merchantRepository.selectOne(merchantWrapper);
        if (merchantEntity == null || !MerchantApplyStatus.APPROVED.getCode().equals(merchantEntity.getApplyStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "当前用户不是已审核通过商家");
        }

        LambdaQueryWrapper<ShopEntity> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(ShopEntity::getMerchantId, merchantEntity.getId());
        shopWrapper.eq(ShopEntity::getShopType, "MERCHANT");
        ShopEntity shopEntity = shopRepository.selectOne(shopWrapper);
        if (shopEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商家店铺不存在");
        }
        return shopEntity;
    }
}
