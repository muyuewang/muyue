package com.muyue.common.utils;

import com.muyue.common.core.domain.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 *
 * @author muyue
 */
public class SecurityUtils {

    /** 超级管理员角色标识 */
    public static final String SUPER_ADMIN = "admin";

    /** 超级管理员用户ID */
    public static final Long SUPER_ADMIN_ID = 1L;

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static Long getDeptId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getDeptId();
    }

    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    /**
     * 是否为超级管理员
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && SUPER_ADMIN_ID.equals(userId);
    }

    public static boolean isAdmin() {
        return isAdmin(getUserId());
    }
}
