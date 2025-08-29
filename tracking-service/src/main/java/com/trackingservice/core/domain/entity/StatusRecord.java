package com.trackingservice.core.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.trackingservice.core.domain.constant.TxnStatus;
import lombok.*;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusRecord {
    private String requestId;
    private String idempotentKey;

    private String sourceService;
    private String targetService;

    private TxnStatus status;
    private String error;
    private JsonNode details;

    private Instant createdAt;
    private Instant updatedAt;
}