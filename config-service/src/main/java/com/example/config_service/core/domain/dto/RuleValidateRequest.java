package com.example.config_service.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RuleValidateRequest extends RuleGetDto {
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
