package com.dedupservice.infrastructure.factory;

import com.dedupservice.core.port.store.DedupStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.dedupservice.infrastructure.store.CaffeineDedupStore;
import com.dedupservice.infrastructure.store.InMemoryDedupStore;
import com.dedupservice.infrastructure.store.RedisDedupStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class DedupCacheFactory {


    private final DedupProperties props;
    private final InMemoryDedupStore inMemory;
    private final CaffeineDedupStore caffeine;
    private final RedisDedupStore redis;


    @Bean
    @Primary
    public DedupStore dedupStore() {
        return switch (props.getStore().toLowerCase()) {
            case "caffeine" -> caffeine;
            case "redis" -> redis;
            default -> inMemory;
        };
    }
}
