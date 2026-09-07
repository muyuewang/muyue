package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.config.DbTypeHolder;
import com.muyue.common.core.domain.entity.SysRole;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.mapper.SysRoleMapper;
import com.muyue.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 角色服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final DbTypeHolder dbTypeHolder;

    @Override
    public Page<SysRole> selectRolePage(Page<SysRole> page, SysRole role) {
        List<SysRole> list = baseMapper.selectRoleList(page, role);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public List<SysRole> selectRoleAll() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getRoleSort));
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        List<SysRole> userRoles = baseMapper.selectRolesByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles) {
            for (SysRole userRole : userRoles) {
                if (role.getRoleId().equals(userRole.getRoleId())) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    @Override
    public Set<String> selectRoleKeysByUserId(Long userId) {
        return baseMapper.selectRoleKeysByUserId(userId);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public boolean checkRoleNameUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole info = baseMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, role.getRoleName())
                .last(dbTypeHolder.limitOne()));
        return info == null || info.getRoleId().equals(roleId);
    }

    @Override
    public boolean checkRoleKeyUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole info = baseMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey())
                .last(dbTypeHolder.limitOne()));
        return info == null || info.getRoleId().equals(roleId);
    }

    @Override
    public void checkRoleAllowed(SysRole role) {
        if (role.getRoleId() != null && SecurityUtils.SUPER_ADMIN_ID.equals(role.getRoleId())) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        if (!checkRoleNameUnique(role)) {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!checkRoleKeyUnique(role)) {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        int rows = baseMapper.insert(role);
        insertRoleMenu(role);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        if (!checkRoleNameUnique(role)) {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!checkRoleKeyUnique(role)) {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        int rows = baseMapper.updateById(role);
        baseMapper.deleteRoleMenuByRoleId(role.getRoleId());
        insertRoleMenu(role);
        return rows > 0;
    }

    @Override
    public boolean updateRoleStatus(SysRole role) {
        checkRoleAllowed(role);
        SysRole update = new SysRole();
        update.setRoleId(role.getRoleId());
        update.setStatus(role.getStatus());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        for (Long roleId : roleIds) {
            SysRole role = baseMapper.selectById(roleId);
            if (role == null) {
                continue;
            }
            checkRoleAllowed(role);
            if (baseMapper.countUserRoleByRoleId(roleId) > 0) {
                throw new ServiceException("角色'" + role.getRoleName() + "'已分配给用户，不能删除");
            }
        }
        baseMapper.deleteRoleMenus(roleIds);
        baseMapper.deleteUserRoleByRoleIds(roleIds);
        return baseMapper.delete(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds)) > 0;
    }

    /**
     * 新增角色菜单关系
     */
    private void insertRoleMenu(SysRole role) {
        Long[] menuIds = role.getMenuIds();
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        for (Long menuId : menuIds) {
            if (menuId != null) {
                baseMapper.insertRoleMenu(role.getRoleId(), menuId);
            }
        }
    }
}
