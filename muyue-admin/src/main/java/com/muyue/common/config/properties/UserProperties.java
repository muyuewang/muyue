package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用户默认配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.user")
public class UserProperties {

    /** 初始化/重置时的默认密码 */
    private String defaultPassword = "admin123";
}
