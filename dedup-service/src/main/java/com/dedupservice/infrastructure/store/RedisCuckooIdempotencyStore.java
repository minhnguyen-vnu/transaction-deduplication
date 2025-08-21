package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCuckooIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;

    @Value("${dedup.cuckoo.filter-name:dedup:cf:v1}")
    private String filterName;

    @Value("${dedup.cuckoo.key-prefix:dedup:}")
    private String keyPrefix;

    private String withPrefix(String key) {
        return keyPrefix + key;
    }

    @Override
    public boolean isDuplicate(String idemKey) {
        return Boolean.TRUE.equals(redis.execute(connection -> {
            byte[] rawFilter = redis.getStringSerializer().serialize(filterName);
            byte[] rawItem = redis.getStringSerializer().serialize(withPrefix(idemKey));
            Object result = connection.execute("CF.EXISTS", rawFilter, rawItem);
            return result != null && ((Long) result) == 1L;
        }, true));
    }

    @Override
    public void recordProcessed(String idemKey) {
        redis.execute(connection -> {
            byte[] rawFilter = redis.getStringSerializer().serialize(filterName);
            byte[] rawItem   = redis.getStringSerializer().serialize(withPrefix(idemKey));
            connection.execute("CF.ADD", rawFilter, rawItem);
            return null;
        }, true);
    }

    @Override
    public void remove(String idemKey) {
        redis.execute(connection -> {
            byte[] rawFilter = redis.getStringSerializer().serialize(filterName);
            byte[] rawItem   = redis.getStringSerializer().serialize(withPrefix(idemKey));
            connection.execute("CF.DEL", rawFilter, rawItem);
            return null;
        }, true);
    }
}
