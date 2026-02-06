package com.example.config_service.infrastructure.cache.redis;

import com.example.config_service.core.port.externalcache.ExternalCacheTemplate;
import com.example.config_service.infrastructure.config.RuleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheTemplate implements ExternalCacheTemplate {

    @Qualifier("objectRedisTemplate")
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Qualifier("byteRedisTemplate")
    private final RedisTemplate<String, byte[]> byteRedisTemplate;

    private final RuleProperties properties;

    // ==== Object methods ====
    @Override
    public boolean contains(String key) {
        try {
            return objectRedisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis contains() failed for key={}", key, e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        try {
            Object value = objectRedisTemplate.opsForValue().get(key);
            return Optional.ofNullable((T) value);
        } catch (ClassCastException e) {
            log.error("Redis get() type mismatch for key={}", key, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Redis get() failed for key={}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value) {
        try {
            objectRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(properties.getDefaultTTL()));
        } catch (Exception e) {
            log.error("Redis put() failed for key={}, value={}", key, value, e);
        }
    }

    @Override
    public void put(String key, Object value, long ttlSeconds) {
        try {
            objectRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Redis put() failed for key={}, value={}", key, value, e);
        }
    }

    @Override
    public void putForever(String key, Object value) {
        try {
            objectRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis putForever() failed for key={}, value={}", key, value, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            objectRedisTemplate.delete(key);
            byteRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete() failed for key={}", key, e);
        }
    }

    // ==== Counter methods ====
    @Override
    public long increment(String key) {
        try {
            Long result = objectRedisTemplate.opsForValue().increment(key, 1);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("Redis increment() failed for key={}", key, e);
            return 0L;
        }
    }

    @Override
    public Optional<Long> getCounter(String key) {
        try {
            Object val = objectRedisTemplate.opsForValue().get(key);
            if (val instanceof Long l) return Optional.of(l);
            if (val instanceof Integer i) return Optional.of(i.longValue());
        } catch (Exception e) {
            log.error("Redis getCounter() failed for key={}", key, e);
        }
        return Optional.empty();
    }

    // ==== Versioned methods ====
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getLatest(String key) {
        try {
            Object value = objectRedisTemplate.opsForValue().get(key + ":latest");
            return Optional.ofNullable((T) value);
        } catch (ClassCastException e) {
            log.error("Redis getLatest() type mismatch for key={}", key, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Redis getLatest() failed for key={}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void putVersion(String key, long version, Object value) {
        try {
            objectRedisTemplate.opsForValue().set(key + ":version:" + version, value, Duration.ofSeconds(properties.getDefaultTTL()));
            objectRedisTemplate.opsForValue().set(key + ":latest", value, Duration.ofSeconds(properties.getDefaultTTL()));
        } catch (Exception e) {
            log.error("Redis putVersion() failed for key={}, version={}, value={}", key, version, value, e);
        }
    }

    // ==== Binary methods ====
    @Override
    public void putBytes(String key, byte[] value) {
        try {
            byteRedisTemplate.opsForValue().set(key, value, properties.getDefaultTTL());
        } catch (Exception e) {
            log.error("Redis putBytes() failed for key={}", key, e);
        }
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        try {
            return Optional.ofNullable(byteRedisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Redis getBytes() failed for key={}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean reserve(String key, long ttlSeconds) {
        long ttl = ttlSeconds > 0 ? ttlSeconds : 1;
        Boolean ok = objectRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(ttl));
        return Boolean.TRUE.equals(ok);
    }
}
