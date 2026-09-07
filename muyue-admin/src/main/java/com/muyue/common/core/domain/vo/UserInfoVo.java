package com.muyue.common.core.domain.vo;

import com.muyue.common.core.domain.entity.SysUser;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 登录成功后返回的用户信息
 *
 * @author muyue
 */
@Data
public class UserInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private SysUser user;

    private List<String> roles;

    private Set<String> permissions;
}
