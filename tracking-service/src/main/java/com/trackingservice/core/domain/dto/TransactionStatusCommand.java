package com.trackingservice.core.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.trackingservice.core.domain.constant.TxnStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusCommand {
    private String requestId;
    private String sourceService;
    private String targetService;
    private String idempotentKey;
    private TxnStatus status;
    private JsonNode payload;
}
