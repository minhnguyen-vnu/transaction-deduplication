package com.dedupservice.infrastructure.messaging;

import com.dedupservice.core.port.messaging.MessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessagingAdapter implements MessagingPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "requests";

    @Override
    public void publishOrderCreatedEvent(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
