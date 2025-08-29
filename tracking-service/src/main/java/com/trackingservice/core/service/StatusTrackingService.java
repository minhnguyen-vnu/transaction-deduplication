package com.trackingservice.core.service;

import com.trackingservice.core.domain.constant.TxnStatus;
import com.trackingservice.core.domain.constant.error.PublishFailedException;
import com.trackingservice.core.domain.dto.KeyCleanupRequested;
import com.trackingservice.core.domain.dto.TransactionStatusCommand;
import com.trackingservice.core.domain.entity.StatusRecord;
import com.trackingservice.core.port.messaging.CleanupPublisher;
import com.trackingservice.core.port.store.StatusStore;
import com.trackingservice.infrastructure.store.StatusRecordDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusTrackingService {
    private final StatusStore store;
    private final CleanupPublisher cleanupPublisher;

    public void publishEvent(StatusRecord record) {
        if (TxnStatus.isTerminal(record.getStatus())) {
            cleanupPublisher.publish(
                    KeyCleanupRequested.builder()
                            .requestId(record.getRequestId())
                            .idempotentKey(record.getIdempotentKey())
                            .reason(record.getStatus().name())
                            .build()
            );
        }
    }

    public void handle(TransactionStatusCommand cmd) {
        try {
            var existing = StatusRecord.builder()
                    .requestId(cmd.getRequestId())
                    .idempotentKey(cmd.getIdempotentKey())
                    .sourceService(cmd.getSourceService())
                    .targetService(cmd.getTargetService())
                    .status(cmd.getStatus())
                    .details(cmd.getPayload())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            var saved = store.insert(existing);
            publishEvent(saved);
        } catch (DuplicateKeyException duplicateKeyException) {
            var existing = store.findByIdempotentKey(cmd.getIdempotentKey()).orElse(null);
            if (existing == null) {
                log.warn("DuplicateKey thrown but no record found for key={}", cmd.getIdempotentKey(), duplicateKeyException);
                throw duplicateKeyException;
            } else {
                if (TxnStatus.isTerminal(existing.getStatus())) {
                    return;
                }
                existing.setStatus(cmd.getStatus());
                existing.setDetails(cmd.getPayload());
                existing.setUpdatedAt(Instant.now());
                var saved = store.insert(existing);
                publishEvent(saved);
            }

        }
    }
}
