package com.mall.platform.service;

import com.mall.platform.vo.HealthStatusVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthService {

    /**
     * 组装健康状态返回数据。
     */
    public HealthStatusVO buildHealthStatus() {
        HealthStatusVO healthStatusVO = new HealthStatusVO();
        healthStatusVO.setServiceName("mall-platform-backend");
        healthStatusVO.setStatus("UP");
        healthStatusVO.setCurrentTime(LocalDateTime.now().toString());
        return healthStatusVO;
    }
}
