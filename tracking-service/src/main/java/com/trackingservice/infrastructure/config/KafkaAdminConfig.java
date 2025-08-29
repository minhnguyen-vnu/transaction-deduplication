package com.trackingservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Configuration
public class KafkaAdminConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String bs) {
        return new KafkaAdmin(Map.of("bootstrap.servers", bs));
    }

    @Bean
    public NewTopic inboundTopic(@Value("${tracking.topics.inbound}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public NewTopic cleanupTopic(@Value("${tracking.topics.cleanup}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }
}
