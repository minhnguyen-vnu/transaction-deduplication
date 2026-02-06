package com.example.config_service.infrastructure.config;

import com.example.config_service.core.service.RuleRevoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisKeyspaceConfig {

    private final RuleRevoker ruleRevokeService;

    @RequiredArgsConstructor
    public static class RedisKeyExpiredHandler {
        private final RuleRevoker ruleRevokeService;

        public void onMessage(String body) {
            try {
                if (body.startsWith("rule:")) {
                    String[] parts = body.split(":");
                    if (parts.length >= 2) {
                        String ruleName = parts[1];
                        log.warn("⏰ Rule cache key expired → refreshing {} from DB", ruleName);
                        ruleRevokeService.refreshRule(ruleName);
                    }
                }
            } catch (Exception e) {
                log.error("❌ Error handling Redis keyspace event", e);
            }
        }
    }

    /**
     * MessageListenerAdapter bean
     */
    @Bean
    public MessageListenerAdapter redisExpiredListenerAdapter() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(
                new RedisKeyExpiredHandler(ruleRevokeService),
                "onMessage" // tên phương thức sẽ gọi
        );
        // Sử dụng Java serializer cho POJO
        adapter.setSerializer(RedisSerializer.string());
        return adapter;
    }

    /**
     * RedisMessageListenerContainer bean
     */
    @Bean(name = "redisContainer")
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter redisExpiredListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Đăng ký listener adapter
        container.addMessageListener(redisExpiredListenerAdapter, new PatternTopic("__keyevent@0__:expired"));

        log.info("✅ RedisMessageListenerContainer bean created");
        return container;
    }
}