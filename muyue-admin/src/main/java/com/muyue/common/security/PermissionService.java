package com.muyue.common.security;

import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

/**
 * 自定义权限校验（用法：@PreAuthorize("@ps.hasPermi('system:user:list')")）
 *
 * @author muyue
 */
@Service("ps")
public class PermissionService {

    /** 所有权限标识 */
    private static final String ALL_PERMISSION = "*:*:*";

    /** 超级管理员角色标识 */
    private static final String SUPER_ADMIN = "admin";

    /** 角色前缀 */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * 是否拥有某个权限
     */
    public boolean hasPermi(String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.contains(ALL_PERMISSION) || permissions.contains(permission.trim());
    }

    /**
     * 是否缺少某个权限
     */
    public boolean lacksPermi(String permission) {
        return !hasPermi(permission);
    }

    /**
     * 是否拥有其中任意一个权限
     */
    public boolean hasAnyPermi(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(this::hasPermi);
    }

    /**
     * 是否拥有某个角色
     */
    public boolean hasRole(String role) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getRoles() == null) {
            return false;
        }
        for (String r : loginUser.getRoles()) {
            if (SUPER_ADMIN.equals(r) || r.equals(trimAndStrip(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有其中任意一个角色
     */
    public boolean hasAnyRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(this::hasRole);
    }

    private String trimAndStrip(String role) {
        String r = role == null ? "" : role.trim();
        return r.startsWith(ROLE_PREFIX) ? r.substring(ROLE_PREFIX.length()) : r;
    }
}
