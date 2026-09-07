package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 角色数据访问层
 *
 * @author muyue
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 分页查询角色列表
     */
    List<SysRole> selectRoleList(Page<SysRole> page, @Param("role") SysRole role);

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色标识
     */
    Set<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 新增角色菜单关系（单条，跨库通用）
     */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 删除角色菜单关系
     */
    int deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量删除角色菜单关系
     */
    int deleteRoleMenus(@Param("roleIds") List<Long> roleIds);

    /**
     * 批量删除用户角色关系
     */
    int deleteUserRoleByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 统计角色已分配的用户数量
     */
    long countUserRoleByRoleId(@Param("roleId") Long roleId);
}
