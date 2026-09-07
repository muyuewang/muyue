package com.muyue.web.controller.monitor;

import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.server.Server;
import com.muyue.system.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author muyue
 */
@Slf4j
@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @PreAuthorize("@ps.hasPermi('monitor:server:list')")
    @GetMapping
    public R<Server> getServer() {
        try {
            return R.ok(serverService.getServer());
        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            return R.fail("获取服务器信息失败：" + e.getMessage());
        }
    }
}
