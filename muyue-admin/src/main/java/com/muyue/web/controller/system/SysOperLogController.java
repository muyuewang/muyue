package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysOperLog;
import com.muyue.common.security.PermissionService;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author muyue
 */
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
public class SysOperLogController extends BaseController {

    /** 查看全部日志的权限标识 */
    private static final String VIEW_ALL_PERMISSION = "monitor:operlog:list";

    private final ISysOperLogService operLogService;
    private final PermissionService permissionService;

    /**
     * 查询操作日志：默认只查自己的，拥有 monitor:operlog:list 权限时可查看全部
     */
    @PreAuthorize("@ps.hasPermi('system:operlog:list')")
    @GetMapping("/list")
    public TableDataInfo<SysOperLog> list(SysOperLog operLog) {
        if (!permissionService.hasPermi(VIEW_ALL_PERMISSION)) {
            operLog.setOperName(SecurityUtils.getUsername());
        }
        Page<SysOperLog> page = operLogService.selectOperLogPage(getPage(), operLog);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /**
     * 日志详情
     */
    @PreAuthorize("@ps.hasPermi('system:operlog:list')")
    @GetMapping("/{operId}")
    public R<SysOperLog> getInfo(@PathVariable Long operId) {
        return R.ok(operLogService.selectOperLogById(operId));
    }

    /**
     * 删除操作日志
     */
    @PreAuthorize("@ps.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable List<Long> operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    /**
     * 清空操作日志
     */
    @PreAuthorize("@ps.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
