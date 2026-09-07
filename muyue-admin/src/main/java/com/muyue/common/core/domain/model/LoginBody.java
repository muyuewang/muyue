package com.muyue.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录对象
 *
 * @author muyue
 */
@Data
public class LoginBody implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 用户密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 */
    private String code;

    /** 验证码唯一标识 */
    private String uuid;
}
