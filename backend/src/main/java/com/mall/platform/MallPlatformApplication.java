package com.mall.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.mall.platform.repository")
@SpringBootApplication
public class MallPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPlatformApplication.class, args);
    }
}
