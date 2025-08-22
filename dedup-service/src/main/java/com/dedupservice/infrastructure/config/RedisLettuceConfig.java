package com.dedupservice.infrastructure.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.event.command.CommandStartedEvent;
import io.lettuce.core.event.command.CommandSucceededEvent;
import io.lettuce.core.event.command.CommandFailedEvent;
import io.lettuce.core.event.Event;
import io.lettuce.core.event.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedisLettuceConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        String url = String.format("redis://%s:%s@%s:%d", redisUsername, redisPassword, redisHost, redisPort);
        RedisClient client = RedisClient.create(url);

        EventBus eventBus = client.getResources().eventBus();
        eventBus.get().subscribe((Event event) -> {
            if (event instanceof CommandStartedEvent started) {
                log.info("Redis Command Started: {} args={}",
                        started.getCommand().getType(), started.getCommand().getArgs());
            } else if (event instanceof CommandSucceededEvent succeeded) {
                log.info("Redis Command Succeeded: {}", succeeded.getCommand().getType());
            } else if (event instanceof CommandFailedEvent failed) {
                log.error("Redis Command Failed: {} error={}",
                        failed.getCommand().getType(), failed.getCause().getMessage());
            }
        });

        return client;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect(StringCodec.UTF8);
    }
}
