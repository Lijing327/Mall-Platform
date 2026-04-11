package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增 / 修改收货地址请求参数。
 * userId 由服务端根据登录态写入，客户端可不传。
 */
public class UserAddressSaveDTO {
    private Long userId;

    @NotBlank(message = "receiverName 不能为空")
    @Size(max = 64, message = "receiverName 长度超限")
    private String receiverName;

    @NotBlank(message = "receiverMobile 不能为空")
    @Size(max = 32, message = "receiverMobile 长度超限")
    private String receiverMobile;

    @NotBlank(message = "province 不能为空")
    @Size(max = 64)
    private String province;

    @NotBlank(message = "city 不能为空")
    @Size(max = 64)
    private String city;

    @NotBlank(message = "district 不能为空")
    @Size(max = 64)
    private String district;

    @NotBlank(message = "detailAddress 不能为空")
    @Size(max = 256)
    private String detailAddress;

    private Boolean isDefault;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
