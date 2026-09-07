package com.muyue.common.config;

import com.muyue.common.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 跨域配置：来源由 muyue.cors.allowed-origins 控制。
 * 配置为 "*" 时使用 OriginPatterns 放行任意来源（仅限开发环境）；
 * 生产环境必须配置为具体域名列表（此时为精确匹配，可安全携带凭证）。
 *
 * @author muyue
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private static final String ALL = "*";

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins == null || origins.isEmpty() || (origins.size() == 1 && ALL.equals(origins.get(0)))) {
            // 开发模式：任意来源（OriginPatterns 才能与 allowCredentials 共存）
            configuration.setAllowedOriginPatterns(List.of(ALL));
        } else {
            // 生产模式：精确域名匹配
            configuration.setAllowedOrigins(origins);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
