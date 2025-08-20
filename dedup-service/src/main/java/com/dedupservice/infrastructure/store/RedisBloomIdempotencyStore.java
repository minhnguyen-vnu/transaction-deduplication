package com.dedupservice.infrastructure.store;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisBloomIdempotencyStore {
    private final RedissonClient redisson;
    private final StringRedisTemplate redis;

    @Value("${dedup.ttl-seconds:600}")
    private long ttlSeconds;

    @Value("${dedup.bloom.name:dedup:bf:v1}")
    private String bloomName;

    @Value("${dedup.bloom.expected-insertions:5000000}")
    private long expectedInsertions;

    @Value("${dedup.bloom.fpr:0.01}")
    private double falsePositiveRate;

    private RBloomFilter<String> bloom;

    @PostConstruct
    void init() {
        bloom = redisson.getBloomFilter(bloomName);
        bloom.tryInit(expectedInsertions, falsePositiveRate);
    }

    public boolean isDuplicate(String idemKey) {
        if (!bloom.contains(idemKey)) return false;
        return redis.hasKey(idemKey);
    }

    public void recordProcessed(String idemKey) {
        redis.opsForValue().set(idemKey, "1", Duration.ofSeconds(ttlSeconds));
        bloom.add(idemKey);
    }
}
