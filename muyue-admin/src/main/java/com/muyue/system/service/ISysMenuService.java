package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysMenu;
import com.muyue.common.core.domain.vo.RouterVo;
import com.muyue.common.core.domain.vo.TreeSelect;

import java.util.List;
import java.util.Set;

/**
 * 菜单服务接口
 *
 * @author muyue
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单列表（树形平铺数据）
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 根据用户ID查询可见菜单（平铺列表，M 目录、C 菜单）
     */
    List<SysMenu> selectMenusByUserId(Long userId);

    /**
     * 构建前端路由
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

    /**
     * 构建菜单下拉树
     */
    List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus);

    /**
     * 查询用户权限标识
     */
    Set<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据角色ID查询菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 根据菜单ID查询菜单
     */
    SysMenu selectMenuById(Long menuId);

    /**
     * 是否存在子菜单
     */
    boolean hasChildByMenuId(Long menuId);

    /**
     * 菜单是否已被角色使用
     */
    boolean checkMenuExistRole(Long menuId);

    /**
     * 新增菜单
     */
    boolean insertMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    boolean updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    boolean deleteMenuById(Long menuId);
}
