package com.example.config_service.core.port.externalcache;

import java.util.Optional;

public interface ExternalCacheTemplate {

    boolean contains(String key);

    <T> Optional<T> get(String key);
    // trả về Optional để tránh null
    void put(String key, Object value);

    void put(String key, Object value, long ttlSeconds);

    void putForever(String key, Object value);

    void delete(String key);

    long increment(String key);

    Optional<Long> getCounter(String key);

    <T> Optional<T> getLatest(String key);    // lấy version mới nhất

    <T> void putVersion(String key, long version, T value);

    void putBytes(String key, byte[] value);

    Optional<byte[]> getBytes(String key);

    default Long getCounterValue(String key) {
        return getCounter(key).orElse(null);
    }

    boolean reserve(String key, long ttlSeconds);
}