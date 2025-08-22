package com.dedupservice.core.domain.dto;

import com.dedupservice.core.domain.constants.RequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupCheckRequest {
    @NotNull
    @JsonProperty("requestPayload")
    private JsonNode requestPayload;

    @JsonProperty("idempotentKey")
    private String idempotentKey;

    @JsonProperty("idempotentFields")
    @Builder.Default
    private List<String> idempotentFields = List.of();


    @JsonProperty("ignoredFields")
    @Builder.Default
    private List<String> ignoredFields = List.of();

    private String requestId;
    private String sourceService;
    private String targetService;
    private String method;
    private String endpoint;
}
