package com.muyue.web.controller.monitor;

import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.enums.BusinessType;
import com.muyue.system.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 在线用户监控
 *
 * @author muyue
 */
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class SysUserOnlineController extends BaseController {

    private final OnlineUserService onlineUserService;

    @PreAuthorize("@ps.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String ipaddr,
                              @RequestParam(required = false) String userName) {
        List<Map<String, Object>> list = onlineUserService.list(ipaddr, userName);
        return new TableDataInfo<>(list, list.size());
    }

    @PreAuthorize("@ps.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{token}")
    public R<?> forceLogout(@PathVariable("token") String token) {
        onlineUserService.forceLogout(token);
        return R.ok();
    }
}
