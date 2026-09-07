package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysRole;
import com.muyue.common.enums.BusinessType;
import com.muyue.system.service.ISysRoleService;
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

import java.util.List;

/**
 * 角色管理
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

    private final ISysRoleService roleService;

    /**
     * 角色列表（分页）
     */
    @PreAuthorize("@ps.hasPermi('system:role:list')")
    @GetMapping("/list")
    public TableDataInfo<SysRole> list(SysRole role) {
        Page<SysRole> page = roleService.selectRolePage(getPage(), role);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /**
     * 角色下拉（全部）
     */
    @PreAuthorize("@ps.hasPermi('system:role:list')")
    @GetMapping("/optionselect")
    public R<List<SysRole>> optionselect() {
        return R.ok(roleService.selectRoleAll());
    }

    /**
     * 角色详情
     */
    @PreAuthorize("@ps.hasPermi('system:role:query')")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        return R.ok(roleService.getById(roleId));
    }

    /**
     * 新增角色
     */
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:role:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改角色
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:role:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        return toAjax(roleService.updateRole(role));
    }

    /**
     * 修改角色状态
     */
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:role:edit')")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable List<Long> roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }
}
