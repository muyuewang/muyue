package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyue.common.core.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 菜单数据访问层
 *
 * @author muyue
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询菜单列表（不过滤权限）
     */
    List<SysMenu> selectMenuList(@Param("menu") SysMenu menu);

    /**
     * 根据用户ID查询菜单树
     */
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限标识
     */
    Set<String> selectMenuPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID删除角色菜单关系
     */
    int deleteRoleMenuByMenuId(@Param("menuId") Long menuId);

    /**
     * 统计菜单已被角色关联的数量
     */
    long countRoleMenuByMenuId(@Param("menuId") Long menuId);
}
