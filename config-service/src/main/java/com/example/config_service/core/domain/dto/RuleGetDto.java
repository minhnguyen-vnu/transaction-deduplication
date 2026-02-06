package com.example.config_service.core.domain.dto;

import lombok.Data;

@Data
public class RuleGetDto {
    private String ruleName = "general"; // default
    private Integer version;             // optional
}
