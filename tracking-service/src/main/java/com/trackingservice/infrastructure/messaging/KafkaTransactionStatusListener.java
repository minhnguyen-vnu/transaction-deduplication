package com.trackingservice.infrastructure.messaging;

import com.trackingservice.core.domain.constant.error.PublishFailedException;
import com.trackingservice.core.domain.dto.TransactionStatusCommand;
import com.trackingservice.core.service.StatusTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionStatusListener {
    private final StatusTrackingService service;

    @KafkaListener(
            topics = "${tracking.topics.inbound}",
            groupId = "status-tracking-service",
            concurrency = "3"
    )
    public void onMessage(@Payload TransactionStatusCommand cmd, Acknowledgment ack)  {
        log.info("Status update: reqId={} key={} status={}", cmd.getRequestId(), cmd.getIdempotentKey(), cmd.getStatus());
        try {
            service.handle(cmd);
            ack.acknowledge();
        } catch (Exception e) {
            throw new PublishFailedException("Failed handling status update", e);
        }
    }
}
