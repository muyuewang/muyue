package com.muyue.web.controller.system;

import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.entity.SysDept;
import com.muyue.common.core.domain.vo.TreeSelect;
import com.muyue.common.enums.BusinessType;
import com.muyue.system.service.ISysDeptService;
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
 * 部门管理
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController extends BaseController {

    private final ISysDeptService deptService;

    /**
     * 部门列表
     */
    @PreAuthorize("@ps.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept dept) {
        return R.ok(deptService.selectDeptList(dept));
    }

    /**
     * 部门下拉树
     */
    @PreAuthorize("@ps.hasPermi('system:dept:list')")
    @GetMapping("/treeselect")
    public R<List<TreeSelect>> treeselect(SysDept dept) {
        return R.ok(deptService.buildDeptTreeSelect(deptService.selectDeptList(dept)));
    }

    /**
     * 部门详情
     */
    @PreAuthorize("@ps.hasPermi('system:dept:query')")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        return R.ok(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:dept:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysDept dept) {
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:dept:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysDept dept) {
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:dept:remove')")
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
