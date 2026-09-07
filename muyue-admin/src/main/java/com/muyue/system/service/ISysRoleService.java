package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 *
 * @author muyue
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色
     */
    Page<SysRole> selectRolePage(Page<SysRole> page, SysRole role);

    /**
     * 查询所有角色（下拉用）
     */
    List<SysRole> selectRoleAll();

    /**
     * 查询用户拥有的角色
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 查询用户拥有的角色标识
     */
    Set<String> selectRoleKeysByUserId(Long userId);

    /**
     * 根据角色ID查询菜单ID
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 校验角色名称是否唯一
     */
    boolean checkRoleNameUnique(SysRole role);

    /**
     * 校验角色权限是否唯一
     */
    boolean checkRoleKeyUnique(SysRole role);

    /**
     * 是否允许操作（超级管理员角色不允许删除/停用）
     */
    void checkRoleAllowed(SysRole role);

    /**
     * 新增角色
     */
    boolean insertRole(SysRole role);

    /**
     * 修改角色
     */
    boolean updateRole(SysRole role);

    /**
     * 修改角色状态
     */
    boolean updateRoleStatus(SysRole role);

    /**
     * 批量删除角色
     */
    boolean deleteRoleByIds(List<Long> roleIds);
}
