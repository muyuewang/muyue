package com.muyue.web.controller.system;

import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.entity.SysMenu;
import com.muyue.common.core.domain.vo.TreeSelect;
import com.muyue.common.enums.BusinessType;
import com.muyue.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController extends BaseController {

    private final ISysMenuService menuService;

    /**
     * 菜单列表
     */
    @PreAuthorize("@ps.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        return R.ok(menuService.selectMenuList(menu));
    }

    /**
     * 菜单详情
     */
    @PreAuthorize("@ps.hasPermi('system:menu:query')")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        return R.ok(menuService.selectMenuById(menuId));
    }

    /**
     * 菜单下拉树
     */
    @PreAuthorize("@ps.hasPermi('system:menu:list')")
    @GetMapping("/treeselect")
    public R<List<TreeSelect>> treeselect(SysMenu menu) {
        // 下拉树需要目录 + 菜单，清空默认的菜单类型条件
        menu.setMenuType(null);
        return R.ok(menuService.buildMenuTreeSelect(menuService.selectMenuList(menu)));
    }

    /**
     * 根据角色查询菜单下拉树（回显已授权菜单）
     */
    @PreAuthorize("@ps.hasPermi('system:menu:list')")
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R<Map<String, Object>> roleMenuTreeselect(@PathVariable Long roleId) {
        SysMenu query = new SysMenu();
        query.setMenuType(null);
        Map<String, Object> data = new HashMap<>();
        data.put("menus", menuService.buildMenuTreeSelect(menuService.selectMenuList(query)));
        data.put("checkedKeys", menuService.selectMenuIdsByRoleId(roleId));
        return R.ok(data);
    }

    /**
     * 新增菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:menu:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:menu:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        return toAjax(menuService.deleteMenuById(menuId));
    }
}
