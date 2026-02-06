package com.example.config_service.core.service;

public interface RuleRevoker {
    void refreshAllRules();
    void refreshRule(String ruleName);
}
