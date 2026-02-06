package com.example.config_service.core.service;

import com.example.config_service.core.domain.dto.RuleValidateRequest;
import com.example.config_service.core.domain.dto.RuleValidateResponse;
import com.example.config_service.core.domain.entity.RuleSet;

import java.util.Optional;

public interface RuleService {
    Optional<RuleSet> getRule(String ruleName, Integer version);
    RuleValidateResponse validateRequest(RuleValidateRequest request);
}
