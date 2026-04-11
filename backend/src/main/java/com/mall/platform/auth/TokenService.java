package com.mall.platform.auth;

import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.config.MallAuthProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 简易 HMAC 令牌：payload 为 userId|ROLE|expEpochSec，防伪造与过期。
 */
@Service
public class TokenService {

    private static final String HMAC_ALG = "HmacSHA256";

    private final MallAuthProperties mallAuthProperties;

    public TokenService(MallAuthProperties mallAuthProperties) {
        this.mallAuthProperties = mallAuthProperties;
    }

    public String issueToken(Long userId, UserRole role) {
        long exp = System.currentTimeMillis() / 1000L + mallAuthProperties.getTokenTtlSeconds();
        String payload = userId + "|" + role.name() + "|" + exp;
        String sig = sign(payload);
        return base64Url(payload) + "." + base64Url(sig);
    }

    public AuthPrincipal parseAndValidate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "缺少登录令牌");
        }
        String token = rawToken.trim();
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌格式无效");
        }
        String payload = new String(Base64.getUrlDecoder().decode(token.substring(0, dot)), StandardCharsets.UTF_8);
        String sig = new String(Base64.getUrlDecoder().decode(token.substring(dot + 1)), StandardCharsets.UTF_8);
        String expected = sign(payload);
        if (!constantTimeEquals(sig, expected)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌无效");
        }
        String[] parts = payload.split("\\|");
        if (parts.length != 3) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌内容无效");
        }
        long exp;
        try {
            exp = Long.parseLong(parts[2]);
        } catch (NumberFormatException ex) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌已损坏");
        }
        if (exp < System.currentTimeMillis() / 1000L) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌已过期");
        }
        long userId;
        try {
            userId = Long.parseLong(parts[0]);
        } catch (NumberFormatException ex) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌已损坏");
        }
        UserRole role;
        try {
            role = UserRole.valueOf(parts[1]);
        } catch (IllegalArgumentException ex) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "令牌角色无效");
        }
        return new AuthPrincipal(userId, role);
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            byte[] keyBytes = mallAuthProperties.getTokenSecret().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(keyBytes, HMAC_ALG));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC 初始化失败", e);
        }
    }

    private static String base64Url(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= x[i] ^ y[i];
        }
        return r == 0;
    }
}
