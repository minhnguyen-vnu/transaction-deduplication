package com.example.config_service.infrastructure.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Value("${caffeine.max-size: 100}")
    private Long caffeineMaxSize;

    @Value("${caffeine.expire-after-write-seconds: 86400000}")
    private Long caffeineExpireAfterWriteSeconds;

    @Bean
    public Cache<String, KieBase> kieBaseCache() {
        return Caffeine.newBuilder()
                .maximumSize(caffeineMaxSize)                // giới hạn số KieBase lưu
                .expireAfterWrite(caffeineExpireAfterWriteSeconds, TimeUnit.SECONDS) // TTL
                .build();
    }
}
