package com.mall.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mall.auth")
public class MallAuthProperties {

    /**
     * HMAC 密钥，生产环境务必通过环境变量覆盖。
     */
    private String tokenSecret = "mall-mvp-change-me-in-prod-min-32-chars!!";

    /**
     * MVP 登录口令（所有用户共用，仅用于演示环境隔离未授权访问）。
     */
    private String loginPassword = "mvp-demo";

    /**
     * 拥有管理端权限的用户 ID 列表。
     */
    private List<Long> adminUserIds = new ArrayList<>(List.of(1L));

    /**
     * 令牌有效期（秒）。
     */
    private long tokenTtlSeconds = 259200L;

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public List<Long> getAdminUserIds() {
        return adminUserIds;
    }

    public void setAdminUserIds(List<Long> adminUserIds) {
        this.adminUserIds = adminUserIds;
    }

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }
}
