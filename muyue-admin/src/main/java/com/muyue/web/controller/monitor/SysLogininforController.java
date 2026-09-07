package com.muyue.web.controller.monitor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysLogininfor;
import com.muyue.common.enums.BusinessType;
import com.muyue.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/monitor/logininfor")
@RequiredArgsConstructor
public class SysLogininforController extends BaseController {

    private final ISysLogininforService logininforService;

    @PreAuthorize("@ps.hasPermi('monitor:logininfor:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor) {
        Page<SysLogininfor> page = logininforService.selectPage(getPage(), logininfor);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('monitor:logininfor:remove')")
    @DeleteMapping("/{infoIds}")
    public R<?> remove(@PathVariable String infoIds) {
        List<Long> ids = Arrays.stream(infoIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return R.ok(logininforService.removeByIds(ids));
    }

    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ps.hasPermi('monitor:logininfor:remove')")
    @DeleteMapping("/clean")
    public R<?> clean() {
        logininforService.cleanLogininfor();
        return R.ok();
    }
}
