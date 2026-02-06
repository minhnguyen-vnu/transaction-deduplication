package com.dedupservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean(name = "objectRedisTemplate")
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory) {
        try {
            // Tạo ObjectMapper hỗ trợ Java 8 Date/Time
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Tạo serializer với mapper custom
            GenericJackson2JsonRedisSerializer serializer =
                    new GenericJackson2JsonRedisSerializer(mapper);

            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);

            // Key là string
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());

            // Value dùng custom serializer
            template.setValueSerializer(serializer);
            template.setHashValueSerializer(serializer);

            template.afterPropertiesSet();
            log.info("✅ objectRedisTemplate initialized successfully.");
            return template;
        } catch (Exception e) {
            log.error("❌ Failed to initialize objectRedisTemplate", e);
            return null;
        }
    }

    @Bean(name = "byteRedisTemplate")
    public RedisTemplate<String, byte[]> byteRedisTemplate(RedisConnectionFactory connectionFactory) {
        try {
            RedisTemplate<String, byte[]> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(RedisSerializer.byteArray());
            log.info("✅ byteRedisTemplate initialized successfully.");
            return template;
        } catch (Exception e) {
            log.error("❌ Failed to initialize byteRedisTemplate", e);
            return null;
        }
    }
}
