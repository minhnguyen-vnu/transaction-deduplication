package com.trackingservice.core.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyCleanupRequested {
    private String requestId;
    private String idempotentKey;
    private String reason;
}
