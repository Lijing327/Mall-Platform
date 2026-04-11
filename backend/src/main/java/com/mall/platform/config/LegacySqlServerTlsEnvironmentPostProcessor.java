package com.mall.platform.config;

import java.security.Security;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 旧版 SQL Server 仅协商 TLS 1.0/1.1 时，JDK 17 默认会在 {@code jdk.tls.disabledAlgorithms} 中禁用这些协议，
 * 导致 mssql-jdbc 握手失败。仅在配置 {@code mall.tls.legacy-sql-server=true} 时放宽客户端约束（见 application-test.yml）。
 */
public class LegacySqlServerTlsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPERTY_NAME = "mall.tls.legacy-sql-server";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(PROPERTY_NAME, Boolean.class, Boolean.FALSE)) {
            return;
        }
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
        String disabled = Security.getProperty("jdk.tls.disabledAlgorithms");
        if (disabled == null || disabled.isBlank()) {
            return;
        }
        String relaxed = Arrays.stream(disabled.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !isTls10Or11(s))
                .collect(Collectors.joining(", "));
        if (!relaxed.equals(disabled)) {
            Security.setProperty("jdk.tls.disabledAlgorithms", relaxed);
        }
    }

    private static boolean isTls10Or11(String token) {
        return "TLSv1".equalsIgnoreCase(token) || "TLSv1.1".equalsIgnoreCase(token);
    }
}
