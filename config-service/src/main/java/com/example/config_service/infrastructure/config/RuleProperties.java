package com.example.config_service.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rule")
public class RuleProperties {

    /** Rule folder */
    @Value("${rule.folder:rules}")
    private String folder;

    /** Debounce time for file watch */
    @Value("${rule.debounce-milis:5000}")
    private long debounceTimeMs;

    @Value("${rule.default-rule-type:general}")
    private String defaultRuleType;

    @Value("${rule.default-ttl:60000}")
    private Integer defaultTTL;
}