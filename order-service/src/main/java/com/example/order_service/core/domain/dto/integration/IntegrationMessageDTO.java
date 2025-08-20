package com.example.order_service.core.domain.dto.integration;

import com.example.order_service.core.domain.constants.RequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationMessageDTO {
    private String requestId;
    private String sourceService;
    private String targetService;
    private String method;
    private String endpoint;
    private String idempotentKey;
    private RequestStatus status;     // sử dụng RequestStatus.name()
    private Object payload;    // payload có thể là DTO bất kỳ
}
