package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.DedupStore;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDedupStore implements DedupStore {


    private static final class Entry { final Instant firstSeen = Instant.now(); }
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();


    @Override
    public boolean reserve(String key, long ttlSeconds) {
        evictIfExpired(key, ttlSeconds);
        return map.putIfAbsent(key, new Entry()) == null;
    }


    @Override
    public boolean exists(String key) {
        return map.containsKey(key);
    }


    @Override
    public void release(String key) {
        map.remove(key);
    }


    private void evictIfExpired(String key, long ttlSecs) {
        var e = map.get(key);
        if (e == null) return;
        if (Duration.between(e.firstSeen, Instant.now()).getSeconds() > ttlSecs) {
            map.remove(key, e);
        }
    }
}