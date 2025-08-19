package com.example.order_service.infrastructure.cache.local;


import com.example.order_service.core.port.cache.CachePort;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryCacheAdapter implements CachePort {

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }
}