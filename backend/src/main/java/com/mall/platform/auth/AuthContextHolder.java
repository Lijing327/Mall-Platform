package com.mall.platform.auth;

import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;

/**
 * 当前线程的登录态（由拦截器在请求进入时设置，结束时清理）。
 */
public final class AuthContextHolder {

    private static final ThreadLocal<AuthPrincipal> HOLDER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthPrincipal principal) {
        HOLDER.set(principal);
    }

    public static AuthPrincipal get() {
        return HOLDER.get();
    }

    public static Long requireUserId() {
        AuthPrincipal p = HOLDER.get();
        if (p == null || p.getUserId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return p.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
