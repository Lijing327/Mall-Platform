package com.mall.platform.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.platform.common.Result;
import com.mall.platform.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 校验 Authorization Bearer 令牌，写入 {@link AuthContextHolder}；
 * 管理端路径要求 ADMIN 角色。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(HEADER);
        String rawToken = null;
        if (header != null && header.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            rawToken = header.substring(PREFIX.length()).trim();
        }
        try {
            AuthPrincipal principal = tokenService.parseAndValidate(rawToken);
            AuthContextHolder.set(principal);
            String path = request.getRequestURI();
            if (isAdminPath(path) && !principal.isAdmin()) {
                writeJson(response, Result.fail(ResultCode.FORBIDDEN));
                return false;
            }
            return true;
        } catch (Exception ex) {
            Result<Object> body = Result.fail(ResultCode.UNAUTHORIZED.getCode(), ex.getMessage());
            writeJson(response, body);
            return false;
        }
    }

    private static boolean isAdminPath(String path) {
        return path.startsWith("/api/admin");
    }

    private void writeJson(HttpServletResponse response, Result<?> result) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }
}
