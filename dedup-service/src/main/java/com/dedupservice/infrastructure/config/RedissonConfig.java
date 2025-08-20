package com.dedupservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    private final RedisProperties redis;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config cfg = new Config();

        boolean sslEnabled = redis.getSsl() != null && redis.getSsl().isEnabled();
        String scheme = sslEnabled ? "rediss" : "redis";
        String address = scheme + "://" + redis.getHost() + ":" + redis.getPort();

        Duration to = redis.getTimeout() != null ? redis.getTimeout() : Duration.ofSeconds(3);

        SingleServerConfig s = cfg.useSingleServer()
                .setAddress(address)
                .setTimeout((int) to.toMillis())
                .setConnectTimeout(10_000)
                .setKeepAlive(true)
                .setConnectionMinimumIdleSize(8)
                .setConnectionPoolSize(32);

        if (redis.getUsername() != null && !redis.getUsername().isEmpty()) {
            s.setUsername(redis.getUsername());
        }
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            s.setPassword(redis.getPassword());
        }

        return Redisson.create(cfg);
    }
}
