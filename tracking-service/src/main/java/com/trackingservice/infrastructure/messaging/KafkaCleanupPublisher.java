package com.trackingservice.infrastructure.messaging;

import com.trackingservice.core.domain.constant.error.PublishFailedException;
import com.trackingservice.core.domain.dto.KeyCleanupRequested;
import com.trackingservice.core.port.messaging.CleanupPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCleanupPublisher implements CleanupPublisher {
    private final KafkaTemplate<String, KeyCleanupRequested> template;

    @Value("${tracking.topics.cleanup}")
    private String topic;

    @Value("${tracking.cleanup.send-timeout-ms:30000}")
    private long sendTimeoutMs;

    @Override
    public void publish(KeyCleanupRequested event) {
        try {
            SendResult<String, KeyCleanupRequested> res =
                    template.send(topic, event.getIdempotentKey(), event)
                            .get(sendTimeoutMs, TimeUnit.MILLISECONDS);

            log.info("Cleanup -> {} p={} off={}",
                    topic,
                    res.getRecordMetadata().partition(),
                    res.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PublishFailedException("Interrupted while waiting for Kafka ack", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new PublishFailedException("Kafka publish failed/timeout", e);
        }
    }
}
