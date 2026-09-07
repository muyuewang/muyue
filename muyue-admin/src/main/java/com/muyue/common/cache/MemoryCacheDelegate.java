package com.muyue.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存实现（单机，默认）。条目按写入时的 TTL 惰性过期。
 *
 * @author muyue
 */
@Component
@ConditionalOnProperty(name = "muyue.cache.type", havingValue = "memory", matchIfMissing = true)
public class MemoryCacheDelegate implements CacheDelegate {

    private record Entry(Object value, long expireAt) {
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value, Duration ttl) {
        store.put(key, new Entry(value, ttl == null ? Long.MAX_VALUE : System.currentTimeMillis() + ttl.toMillis()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Entry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expireAt() < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return (T) entry.value();
    }

    @Override
    public boolean delete(String key) {
        return store.remove(key) != null;
    }

    @Override
    public boolean hasKey(String key) {
        return get(key, Object.class) != null;
    }

    @Override
    public Set<String> keys(String prefix) {
        Set<String> result = new HashSet<>();
        for (String key : store.keySet()) {
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return result;
    }
}
