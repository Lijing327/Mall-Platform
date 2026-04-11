package com.mall.platform.auth;

import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;

/**
 * 将请求体中的 userId 与令牌主体对齐，禁止客户端伪造身份。
 */
public final class AuthBinding {

    private AuthBinding() {
    }

    public static long currentUserIdOrThrow() {
        return AuthContextHolder.requireUserId();
    }

    public static void assertSameUser(Long bodyUserId, long authUserId) {
        if (bodyUserId != null && !bodyUserId.equals(authUserId)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "userId 与登录态不一致");
        }
    }
}
