package com.mall.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MVP 登录（口令 + 用户 ID，用于签发令牌）。
 */
public class LoginDTO {
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotBlank(message = "password 不能为空")
    private String password;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
