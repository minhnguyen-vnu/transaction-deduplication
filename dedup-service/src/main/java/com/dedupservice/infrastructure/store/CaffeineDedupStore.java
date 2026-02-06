package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.DedupStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class CaffeineDedupStore implements DedupStore {

    private final Cache<String, Object> cache;
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public CaffeineDedupStore(DedupProperties props) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(props.getTtlSeconds()))
                .maximumSize(props.getCaffeine().getMaxEntries())
                .build();
    }

    @Override
    public boolean contains(String key) {
        return cache.asMap().containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) cache.getIfPresent(key));
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void put(String key, Object value, long ttlSeconds) {
        // Caffeine không hỗ trợ TTL riêng cho từng entry → chỉ có TTL global
        // Giả lập bằng cách ghi đè và rely on default expireAfterWrite
        cache.put(key, value);
        log.debug("⚠️ Caffeine does not support per-entry TTL ({}s), using global default.", ttlSeconds);
    }

    @Override
    public void putForever(String key, Object value) {
        // "Forever" = bỏ qua expire, nhưng Caffeine TTL global vẫn áp dụng
        cache.policy().expireAfterWrite().ifPresent(policy -> {
            log.warn("⚠️ putForever called but Caffeine has global expireAfterWrite; value may still expire!");
        });
        cache.put(key, value);
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }

    @Override
    public long increment(String key) {
        return counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public Optional<Long> getCounter(String key) {
        return Optional.ofNullable(counters.get(key)).map(AtomicLong::get);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getLatest(String key) {
        // đơn giản: trả về entry thường → production Redis store sẽ implement versioned
        return Optional.ofNullable((T) cache.getIfPresent(key));
    }

    @Override
    public <T> void putVersion(String key, long version, T value) {
        // Với Caffeine demo: chỉ lưu "latest" theo key
        String versionedKey = key + ":v" + version;
        cache.put(versionedKey, value);
        cache.put(key, value); // latest
    }

    @Override
    public void putBytes(String key, byte[] value) {
        cache.put(key, value);
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        return Optional.ofNullable(cache.getIfPresent(key))
                .filter(v -> v instanceof byte[])
                .map(v -> (byte[]) v);
    }

    @Override
    public boolean reserve(String key, long ttlSeconds) {
        return cache.asMap().putIfAbsent(key, Boolean.TRUE) == null;
    }
}