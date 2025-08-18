package com.testing_service.adapter.outbound.stub;

import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDedupRepository {
    private static final class Entry { final Instant firstSeen = Instant.now(); }
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
    private Duration ttl = Duration.ofMinutes(10);

    public void setTtlMinutes(long minutes) { this.ttl = Duration.ofMinutes(minutes); }

    public boolean reserve(String key) {
        evict(key);
        return map.putIfAbsent(key, new Entry()) == null; // winner → allowed
    }
    private void evict(String key) {
        var e = map.get(key);
        if (e != null && Duration.between(e.firstSeen, Instant.now()).compareTo(ttl) > 0) map.remove(key, e);
    }
}
