package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全相关配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.security")
public class SecurityProperties {

    /** 匿名访问地址，逗号分隔 */
    private String anonymous = "/captchaImage,/login";

    /** 排除鉴权地址，逗号分隔 */
    private String excludes = "/*.html,/**/*.html,/**/*.css,/**/*.js,/favicon.ico";

    public String[] getAnonymousArray() {
        return split(anonymous);
    }

    public String[] getExcludesArray() {
        return split(excludes);
    }

    private String[] split(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
