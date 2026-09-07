package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret = "muyueScaffoldSecretKeyForJwtTokenSignMustBeLongEnough2026";

    /** 有效期（分钟） */
    private long expireTime = 720;

    /** 请求头名称 */
    private String header = "Authorization";
}
