package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.DedupStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CaffeineDedupStore implements DedupStore {
    private final Cache<String, Boolean> cache;


    public CaffeineDedupStore(DedupProperties props) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(props.getTtlSeconds()))
                .maximumSize(props.getCaffeine().getMaxEntries())
                .build();
    }

    @Override
    public boolean reserve(String key, long ttlSeconds) {
        return cache.asMap().putIfAbsent(key, Boolean.TRUE) == null;
    }


    @Override
    public boolean exists(String key) {
        return cache.asMap().containsKey(key);
    }


    @Override
    public void release(String key) {
        cache.invalidate(key);
    }
}
