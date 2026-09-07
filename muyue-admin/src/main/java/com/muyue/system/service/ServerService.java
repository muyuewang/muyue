package com.muyue.system.service;

import com.muyue.common.core.domain.server.Server;
import org.springframework.stereotype.Service;

/**
 * 服务器监控服务
 *
 * @author muyue
 */
@Service
public class ServerService {

    /**
     * 采集服务器信息
     */
    public Server getServer() throws Exception {
        Server server = new Server();
        server.copyTo();
        return server;
    }
}
