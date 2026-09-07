package com.muyue.web.service.impl;

import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.core.domain.model.LoginUser;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.service.ISysPermissionService;
import com.muyue.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户认证（Spring Security）
 *
 * @author muyue
 */
@Service("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService userService;
    private final ISysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户'" + username + "'不存在");
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException("账号已停用，请联系管理员");
        }
        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user) {
        return new LoginUser(user, permissionService.getMenuPermission(user), permissionService.getRolePermission(user));
    }
}
