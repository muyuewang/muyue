package com.muyue.common.cache;

import java.time.Duration;
import java.util.Set;

/**
 * 缓存抽象：验证码 / 登录会话的存储委托
 * <p>
 * memory 实现（默认）：单机内存，开箱即跑；
 * redis 实现（muyue.cache.type=redis）：多机部署共享缓存，支持服务端主动作废令牌。
 *
 * @author muyue
 */
public interface CacheDelegate {

    /** 写入缓存（带过期时间） */
    void put(String key, Object value, Duration ttl);

    /** 读取缓存 */
    <T> T get(String key, Class<T> type);

    /** 删除缓存 */
    boolean delete(String key);

    /** 键是否存在 */
    boolean hasKey(String key);

    /** 按前缀匹配所有键 */
    Set<String> keys(String prefix);
}
