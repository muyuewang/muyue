package com.muyue.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置
 *
 * @author muyue
 */
@Data
@Component
@ConfigurationProperties(prefix = "muyue.captcha")
public class CaptchaProperties {

    /** 是否开启验证码 */
    private Boolean enabled = true;

    /** 验证码类型：math 算术、char 字符 */
    private String type = "math";
}
