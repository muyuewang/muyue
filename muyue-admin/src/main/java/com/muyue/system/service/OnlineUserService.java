package com.muyue.system.service;

import com.muyue.common.cache.CacheDelegate;
import com.muyue.common.config.properties.JwtProperties;
import com.muyue.common.core.domain.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在线用户服务（基于 CacheDelegate 的会话白名单）
 * <p>
 * 登录成功 -> 写入 login_tokens:{token}（TTL 与令牌一致）；
 * 校验时 JWT 合法但会话不存在 -> 视为已登出/已强退/已过期；
 * 登出与强退 -> 删除键，多实例部署下即时全节点生效（redis 模式）。
 *
 * @author muyue
 */
@Slf4j
@Service
public class OnlineUserService {

    public static final String SESSION_KEY_PREFIX = "login_tokens:";

    private final CacheDelegate cacheDelegate;
    private final JwtProperties jwtProperties;

    public OnlineUserService(CacheDelegate cacheDelegate, JwtProperties jwtProperties) {
        this.cacheDelegate = cacheDelegate;
        this.jwtProperties = jwtProperties;
    }

    private String key(String token) {
        return SESSION_KEY_PREFIX + token;
    }

    /**
     * 登录成功后登记会话（TTL 与令牌有效期一致）
     */
    public void save(String token, LoginUser user) {
        cacheDelegate.put(key(token), user, Duration.ofMinutes(jwtProperties.getExpireTime()));
    }

    /**
     * 会话是否存在（JWT 合法但会话缺失 = 已被主动作废）
     */
    public boolean hasSession(String token) {
        return cacheDelegate.hasKey(key(token));
    }

    /**
     * 移除会话（登出）
     */
    public void remove(String token) {
        cacheDelegate.delete(key(token));
    }

    /**
     * 强制下线（与登出等价：删除会话键）
     */
    public void forceLogout(String token) {
        boolean removed = cacheDelegate.delete(key(token));
        if (removed && token != null && token.length() > 16) {
            log.warn("强制下线 token={}", token.substring(0, 16));
        }
    }

    /**
     * 查询在线用户（可按 IP / 用户名模糊匹配）
     */
    public List<Map<String, Object>> list(String ipaddr, String userName) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> keys = cacheDelegate.keys(SESSION_KEY_PREFIX);
        for (String key : keys) {
            LoginUser user = cacheDelegate.get(key, LoginUser.class);
            if (user == null) {
                continue;
            }
            String name = user.getUsername();
            String ip = user.getIpaddr();
            if (ipaddr != null && !ipaddr.isEmpty() && (ip == null || !ip.contains(ipaddr))) {
                continue;
            }
            if (userName != null && !userName.isEmpty() && (name == null || !name.contains(userName))) {
                continue;
            }
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("tokenId", key.substring(SESSION_KEY_PREFIX.length()));
            map.put("userName", name);
            map.put("ipaddr", ip);
            map.put("loginTime", user.getLoginTime());
            map.put("deptName", user.getUser() != null ? user.getUser().getDept() : null);
            result.add(map);
        }
        return result;
    }

    /**
     * 在线数量
     */
    public int size() {
        return cacheDelegate.keys(SESSION_KEY_PREFIX).size();
    }
}
