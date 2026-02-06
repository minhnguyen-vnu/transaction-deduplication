package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.DedupStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryDedupStore implements DedupStore {

    private static final class Entry {
        final Instant firstSeen = Instant.now();
        final Object value;
        Entry(Object value) { this.value = value; }
    }

    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable(map.get(key)).map(e -> (T) e.value);
    }

    @Override
    public void put(String key, Object value) {
        map.put(key, new Entry(value));
    }

    @Override
    public void put(String key, Object value, long ttlSeconds) {
        // TTL chỉ check khi get/reserve → không có scheduler cleanup
        map.put(key, new Entry(value));
        log.debug("InMemoryDedupStore put with TTL={}s (checked lazily).", ttlSeconds);
    }

    @Override
    public void putForever(String key, Object value) {
        map.put(key, new Entry(value)); // forever = không expire
    }

    @Override
    public void delete(String key) {
        map.remove(key);
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
        return Optional.ofNullable(map.get(key)).map(e -> (T) e.value);
    }

    @Override
    public <T> void putVersion(String key, long version, T value) {
        map.put(key + ":v" + version, new Entry(value));
        map.put(key, new Entry(value)); // latest
    }

    @Override
    public void putBytes(String key, byte[] value) {
        map.put(key, new Entry(value));
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        return Optional.ofNullable(map.get(key))
                .map(e -> e.value)
                .filter(v -> v instanceof byte[])
                .map(v -> (byte[]) v);
    }

    @Override
    public boolean reserve(String key, long ttlSeconds) {
        evictIfExpired(key, ttlSeconds);
        return map.putIfAbsent(key, new Entry(Boolean.TRUE)) == null;
    }

    private void evictIfExpired(String key, long ttlSecs) {
        var e = map.get(key);
        if (e == null) return;
        if (Duration.between(e.firstSeen, Instant.now()).getSeconds() > ttlSecs) {
            map.remove(key, e);
        }
    }
}