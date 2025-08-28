package com.dedupservice.core.domain.dto;


import com.dedupservice.core.domain.constants.RequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private String requestId;
    private String sourceService;
    private String targetService;
    private String method;
    private String endpoint;
    private String idempotentKey;
    private RequestStatus status;
    private Object payload;
}
