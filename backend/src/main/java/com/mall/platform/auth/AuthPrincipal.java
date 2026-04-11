package com.mall.platform.auth;

/**
 * 当前请求登录主体（由令牌解析得到）。
 */
public class AuthPrincipal {
    private final Long userId;
    private final UserRole role;

    public AuthPrincipal(Long userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
