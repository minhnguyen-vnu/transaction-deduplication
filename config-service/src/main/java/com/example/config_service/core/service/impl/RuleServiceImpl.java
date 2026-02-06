package com.example.config_service.core.service.impl;

import com.example.config_service.core.domain.dto.RuleBuildResult;
import com.example.config_service.core.domain.dto.RuleValidateRequest;
import com.example.config_service.core.domain.dto.RuleValidateResponse;
import com.example.config_service.core.domain.entity.RuleSet;
import com.example.config_service.core.service.RuleService;
import com.example.config_service.core.service.RuleTester;
import com.example.config_service.infrastructure.cache.redis.RedisCacheTemplate;
import com.example.config_service.infrastructure.store.repository.RuleSetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleServiceImpl implements RuleService {

    private final RuleSetRepository ruleSetRepository;
    private final RedisCacheTemplate redisCacheTemplate;
    private final ObjectMapper objectMapper;
    private final RuleTester ruleTester;


    @Override
    public Optional<RuleSet> getRule(String ruleName, Integer version) {
        String key = (version == null)
                ? String.format("rule:%s:latest", ruleName)
                : String.format("rule:%s:%d", ruleName, version);

        // 1. try cache
        Optional<Object> cached = redisCacheTemplate.get(key);
        if (cached.isPresent()) {
            Object raw = cached.get();
            RuleSet ruleSet;
            if (raw instanceof RuleSet) {
                ruleSet = (RuleSet) raw;
            } else if (raw instanceof LinkedHashMap) {
                // convert LinkedHashMap -> RuleSet
                ruleSet = objectMapper.convertValue(raw, RuleSet.class);
            } else {
                log.warn("Unexpected cached type for key={}, type={}", key, raw.getClass());
                ruleSet = null;
            }

            if (ruleSet != null) {
                log.info("Cache hit for key={}", key);
                return Optional.of(ruleSet);
            }
        }

        log.info("Cache miss for key={}, start querying from DB", key);

        // 2. fallback repo
        Optional<RuleSet> fromDb = (version == null)
                ? ruleSetRepository.findTopByRuleNameAndIsActiveTrueOrderByVersionDesc(ruleName)
                : ruleSetRepository.findByRuleNameAndVersion(ruleName, version);

        // 3. put to cache
        fromDb.ifPresent(rule -> {
            try {
                redisCacheTemplate.put(key, rule);
            } catch (Exception e) {
                log.error("Failed to cache rule key={}", key, e);
            }
        });

        return fromDb;
    }

    @Override
    public RuleValidateResponse validateRequest(RuleValidateRequest request) {
        Optional<RuleSet> ruleSetOpt = getRule(request.getRuleName(), request.getVersion());
        if (ruleSetOpt.isEmpty()) {
            return RuleValidateResponse.builder()
                    .success(false)
                    .message("Rule not found: " + request.getRuleName() + " (version=" + request.getVersion() + ")")
                    .build();
        }

        RuleSet ruleSet = ruleSetOpt.get();

        RuleBuildResult result = ruleTester.fireFromDrl(ruleSet.getDrl(), request.getRuleName(), request);

        return RuleValidateResponse.builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .build();
    }
}
