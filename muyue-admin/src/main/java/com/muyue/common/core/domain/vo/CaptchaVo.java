package com.muyue.common.core.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码响应对象
 *
 * @author muyue
 */
@Data
public class CaptchaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否开启验证码 */
    private Boolean captchaEnabled;

    /** 验证码唯一标识 */
    private String key;

    /** base64 图片 */
    private String img;
}
