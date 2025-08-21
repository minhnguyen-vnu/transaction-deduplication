package com.dedupservice.infrastructure.factory;

import com.dedupservice.core.port.store.IdempotencyStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.dedupservice.infrastructure.store.RedisBloomIdempotencyStore;
import com.dedupservice.infrastructure.store.RedisCuckooIdempotencyStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class IdempotencyStoreFactory {

    private final RedisBloomIdempotencyStore bloomStore;
    private final RedisCuckooIdempotencyStore cuckooStore;
    private final DedupProperties props;

    @Bean
    public IdempotencyStore idempotencyStore() {
        String type = props.getIdempotent_type();
        if (type != null && type.equalsIgnoreCase("cuckoo")) {
            return cuckooStore;
        } else {
            return bloomStore;
        }
    }
}