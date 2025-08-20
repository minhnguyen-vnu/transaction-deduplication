package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.DedupStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisDedupStore implements DedupStore {


    private final StringRedisTemplate redis;


    @Override
    public boolean reserve(String key, long ttlSeconds) {
        long ttl = ttlSeconds > 0 ? ttlSeconds : 1;
        Boolean ok = redis.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(ttl));
        return Boolean.TRUE.equals(ok);
    }


    @Override
    public boolean exists(String key) {
        return redis.hasKey(key);
    }


    @Override
    public void release(String key) {
        redis.delete(key);
    }
}