package com.dedupservice.core.service;

import com.dedupservice.core.domain.constants.RequestStatus;
import com.dedupservice.core.domain.dto.DedupCheckRequest;
import com.dedupservice.core.domain.dto.DedupResult;
import com.dedupservice.core.domain.dto.MessageDTO;
import com.dedupservice.core.port.messaging.MessagingPort;
import com.dedupservice.core.port.store.DedupStore;
import com.dedupservice.core.port.store.IdempotencyStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.dedupservice.kernel.utils.KeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DedupUseCase {

    private final Canonicalizer canonicalizer;
    private final DedupStore store;
    private final DedupProperties props;
    private final IdempotencyStore idempotencyStore;
    private final MessagingPort messagingPort;

    public DedupResult check(DedupCheckRequest req) {
        try {
            String keyMaterial;

            if (StringUtils.hasText(req.getIdempotentKey())) {
                keyMaterial = req.getIdempotentKey().trim();
            } else {
                keyMaterial = canonicalizer.buildCanonical(
                        req.getRequestPayload(),
                        req.getIdempotentFields()
                );
            }

            String storeKey = KeyUtil.sha256Hex(keyMaterial);

            boolean doesExist = idempotencyStore.isDuplicate(storeKey);
            if (!doesExist) {
                // chưa tồn tại → mark processed + publish event
                idempotencyStore.recordProcessed(storeKey);

                // publish async status PENDING
                asyncPublish(req);

                return DedupResult.allow();
            } else {
                // đã tồn tại trong idempotency → kiểm tra store
                boolean ok = store.reserve(storeKey, props.getTtlSeconds());
                if (ok) {
                    // vẫn cho qua và publish async
                    asyncPublish(req);
                    return DedupResult.allow();
                } else {
                    return DedupResult.duplicate("FOUND_IN_STORE");
                }
            }

        } catch (Exception e) {
            log.error("Dedup check failed for request={}, error={}", req, e.getMessage(), e);
            return DedupResult.reject("Dedup check failed: " + e.getMessage());
        }
    }

    private void asyncPublish(DedupCheckRequest request) {
        CompletableFuture.runAsync(() -> {
            try {
                publishEvent(request);
            } catch (Exception ex) {
                log.error("Failed to publish dedup event async, requestId={}, error={}",
                        request.getRequestId(), ex.getMessage(), ex);
            }
        });
    }

    private void publishEvent(DedupCheckRequest request) {
        MessageDTO msg = MessageDTO.builder()
                .requestId(request.getRequestId())
                .sourceService(request.getSourceService())
                .targetService(request.getTargetService())
                .method(request.getMethod())
                .endpoint(request.getEndpoint())
                .idempotentKey(request.getIdempotentKey())
                .status(RequestStatus.PENDING)
                .payload(request.getRequestPayload())
                .build();

        messagingPort.publishOrderCreatedEvent(msg.toString()); // stringify JSON nếu cần
        log.info("Published dedup event async: {}", msg);
    }
}
