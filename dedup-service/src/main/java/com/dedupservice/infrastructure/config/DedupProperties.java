package com.dedupservice.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dedup")
public class DedupProperties {
    private String store = "memory";
    private long ttlSeconds = 600;


    private Caffeine caffeine = new Caffeine();


    @Data
    public static class Caffeine {
        private long maxEntries = 100_000L;
    }
}