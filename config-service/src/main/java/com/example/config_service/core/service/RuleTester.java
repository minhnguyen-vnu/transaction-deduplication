package com.example.config_service.core.service;

import com.example.config_service.core.domain.dto.RuleBuildResult;

public interface RuleTester {
    RuleBuildResult testBuildFromDrl(String drl);
    RuleBuildResult testFromDb(Long ruleId);
    RuleBuildResult testFromRedis(String key);
    RuleBuildResult fireFromDrl(String drl, String ruleName, Object... facts);
}
