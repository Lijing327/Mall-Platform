package com.mall.platform.controller;

import com.mall.platform.common.Result;
import com.mall.platform.service.HealthService;
import com.mall.platform.vo.HealthStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * 基础健康检查接口，用于验证项目可运行。
     */
    @GetMapping
    public Result<HealthStatusVO> health() {
        return Result.success(healthService.buildHealthStatus());
    }
}
