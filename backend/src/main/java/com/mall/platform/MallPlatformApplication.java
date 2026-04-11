package com.mall.platform;

import com.mall.platform.config.MallAuthProperties;
import com.mall.platform.config.MallFeatureProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.mall.platform.repository")
@SpringBootApplication
@EnableConfigurationProperties({MallAuthProperties.class, MallFeatureProperties.class})
public class MallPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPlatformApplication.class, args);
    }
}
