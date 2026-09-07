package com.muyue.system.service.impl;

import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.mapper.SysMenuMapper;
import com.muyue.system.mapper.SysRoleMapper;
import com.muyue.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现（角色/菜单权限标识）
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements ISysPermissionService {

    private static final String ALL_PERMISSION = "*:*:*";
    private static final String SUPER_ADMIN_ROLE = "admin";

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public Set<String> getMenuPermission(SysUser user) {
        if (SecurityUtils.isAdmin(user.getUserId())) {
            return Set.of(ALL_PERMISSION);
        }
        Set<String> perms = menuMapper.selectMenuPermsByUserId(user.getUserId());
        return perms == null ? Set.of() : perms;
    }

    @Override
    public List<String> getRolePermission(SysUser user) {
        if (SecurityUtils.isAdmin(user.getUserId())) {
            return List.of(SUPER_ADMIN_ROLE);
        }
        return new ArrayList<>(roleMapper.selectRoleKeysByUserId(user.getUserId()));
    }
}
