package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.config.properties.UserProperties;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysRole;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.utils.StringUtils;
import com.muyue.system.service.ISysRoleService;
import com.muyue.system.service.ISysUserService;
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
 * 用户管理
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final UserProperties userProperties;

    /**
     * 用户列表（分页）
     */
    @PreAuthorize("@ps.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser user) {
        Page<SysUser> page = userService.selectUserPage(getPage(), user);
        for (SysUser record : page.getRecords()) {
            record.setPassword(null);
        }
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /**
     * 用户详情（含角色信息）
     */
    @PreAuthorize("@ps.hasPermi('system:user:query')")
    @GetMapping("/{userId}")
    public R<Map<String, Object>> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", roles);
        data.put("roleIds", roles.stream().filter(SysRole::isFlag).map(SysRole::getRoleId).toList());
        return R.ok(data);
    }

    /**
     * 新增用户
     */
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:user:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(userProperties.getDefaultPassword());
        }
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:user:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        user.setPassword(null);
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:user:remove')")
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable List<Long> userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 修改状态
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:user:edit')")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 重置密码
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:user:resetPwd')")
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        String password = StringUtils.isBlank(user.getPassword())
                ? userProperties.getDefaultPassword() : user.getPassword();
        return toAjax(userService.resetUserPassword(user.getUserId(), password));
    }
}
