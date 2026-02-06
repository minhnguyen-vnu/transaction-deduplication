package com.example.config_service.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleBuildResult {
    private String ruleType;
    private int version;
    private boolean success;
    private String message;

    public static RuleBuildResult success() {
        return RuleBuildResult.builder()
                .success(true)
                .message(null)
                .build();
    }

    public static RuleBuildResult success(String message) {
        return RuleBuildResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static RuleBuildResult fail(String message) {
        return RuleBuildResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}