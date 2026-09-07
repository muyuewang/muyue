package com.muyue.system.service;

import com.muyue.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 *
 * @author muyue
 */
public interface ISysPermissionService {

    /**
     * 获取菜单权限标识（超级管理员返回 *:*:*）
     */
    Set<String> getMenuPermission(SysUser user);

    /**
     * 获取角色权限标识（超级管理员返回 admin）
     */
    List<String> getRolePermission(SysUser user);
}
