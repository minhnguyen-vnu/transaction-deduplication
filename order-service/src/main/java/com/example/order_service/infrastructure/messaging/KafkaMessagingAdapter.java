package com.example.order_service.infrastructure.messaging;


import com.example.order_service.core.port.messaging.MessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessagingAdapter implements MessagingPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "order-created";

    @Override
    public void publishOrderCreatedEvent(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
