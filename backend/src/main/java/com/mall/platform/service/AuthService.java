package com.mall.platform.service;

import com.mall.platform.auth.TokenService;
import com.mall.platform.auth.UserRole;
import com.mall.platform.common.BizException;
import com.mall.platform.common.ResultCode;
import com.mall.platform.config.MallAuthProperties;
import com.mall.platform.dto.LoginDTO;
import com.mall.platform.vo.LoginVO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MallAuthProperties mallAuthProperties;
    private final TokenService tokenService;

    public AuthService(MallAuthProperties mallAuthProperties, TokenService tokenService) {
        this.mallAuthProperties = mallAuthProperties;
        this.tokenService = tokenService;
    }

    public LoginVO login(LoginDTO loginDTO) {
        if (!mallAuthProperties.getLoginPassword().equals(loginDTO.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "账号或密码错误");
        }
        UserRole role = mallAuthProperties.getAdminUserIds().contains(loginDTO.getUserId())
                ? UserRole.ADMIN
                : UserRole.USER;
        String token = tokenService.issueToken(loginDTO.getUserId(), role);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(loginDTO.getUserId());
        vo.setRole(role.name());
        return vo;
    }
}
