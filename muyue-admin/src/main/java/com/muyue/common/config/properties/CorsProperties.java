package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 跨域配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.cors")
public class CorsProperties {

    /**
     * 允许的跨域来源。
     * "*" 表示放行任意来源（仅限开发环境）；生产必须配置为具体域名列表，如：
     *   allowed-origins: https://admin.example.com,https://www.example.com
     */
    private List<String> allowedOrigins = List.of("*");

    /**
     * 是否允许携带凭证（Cookie / Authorization 头）
     */
    private boolean allowCredentials = true;
}
