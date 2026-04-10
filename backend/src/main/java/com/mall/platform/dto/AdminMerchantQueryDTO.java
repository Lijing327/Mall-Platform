package com.mall.platform.dto;

/**
 * 管理员商家列表查询参数。
 */
public class AdminMerchantQueryDTO extends BasePageQueryDTO {
    private String applyStatus;
    private String keyword;

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
