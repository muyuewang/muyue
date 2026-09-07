package com.muyue.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis 缓存实现（多机部署）：值统一存 JSON，跨实例共享验证码与登录会话。
 *
 * @author muyue
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "muyue.cache.type", havingValue = "redis")
public class RedisCacheDelegate implements CacheDelegate {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            String json = value instanceof String s ? s : objectMapper.writeValueAsString(value);
            if (ttl != null) {
                redisTemplate.opsForValue().set(key, json, ttl);
            } else {
                redisTemplate.opsForValue().set(key, json);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Redis 写入失败：" + e.getMessage(), e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            if (type == String.class) {
                return (T) json;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Redis 反序列化失败 key={}：{}", key, e.getMessage());
            redisTemplate.delete(key);
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Set<String> keys(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys == null ? new HashSet<>() : keys;
    }
}
