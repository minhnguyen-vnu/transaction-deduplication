package com.trackingservice.infrastructure.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.trackingservice.core.domain.constant.TxnStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("txn_status_records")
public class StatusRecordDocument {
    @Id
    private String idempotentKey;

    private String requestId;
    private String sourceService;
    private String targetService;
    private TxnStatus status;
    private String error;
    private Map<String, Object> details;
    private Instant createdAt;
    private Instant updatedAt;
}
