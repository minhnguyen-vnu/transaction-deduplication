package com.example.config_service.core.service.impl;

import com.example.config_service.core.domain.entity.RuleSet;
import com.example.config_service.core.service.RuleRevoker;
import com.example.config_service.infrastructure.cache.redis.RedisCacheTemplate;
import com.example.config_service.infrastructure.store.repository.RuleSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleRevokerImpl implements RuleRevoker {

    private final RuleSetRepository ruleSetRepository;
    private final RedisCacheTemplate redisCache; // giả sử bạn có wrapper sẵn

    @Value("${rule.cache.threshold:3}")
    private int threshold;

    @Transactional(readOnly = true)
    public void refreshAllRules() {
        log.info("♻️ Refreshing all rules from DB into Redis cache (threshold={})", threshold);

        // Lấy danh sách tất cả ruleName distinct
        List<String> ruleNames = ruleSetRepository.findDistinctRuleNames();
        for (String ruleName : ruleNames) {
            refreshRule(ruleName);
        }
    }

    /**
     * Refresh cache cho 1 rule cụ thể.
     */
    @Transactional(readOnly = true)
    public void refreshRule(String ruleName) {
        log.info("♻️ Refreshing rule {} from DB into Redis cache", ruleName);

        try {
            // Lấy N version mới nhất từ DB
            List<RuleSet> latestVersions = ruleSetRepository.findTopNByRuleNameOrderByVersionDesc(ruleName, threshold);

            if (latestVersions.isEmpty()) {
                log.warn("⚠️ No versions found for rule {}", ruleName);
                return;
            }

            // Cache từng version
            for (RuleSet ruleSet : latestVersions) {
                String key = "rule:" + ruleSet.getRuleName() + ":" + ruleSet.getVersion();
                redisCache.put(key, ruleSet);
                log.info("✅ Cached {} v{}", ruleSet.getRuleName(), ruleSet.getVersion());
            }

            // Cache latest (version đầu tiên trong list vì đã sort desc)
            RuleSet latest = latestVersions.get(0);
            String latestKey = "rule:" + latest.getRuleName() + ":latest";
            redisCache.put(latestKey, latest);
            log.info("✅ Cached {}:latest → v{}", latest.getRuleName(), latest.getVersion());

        } catch (Exception e) {
            log.error("❌ Failed to refresh cache for rule {}", ruleName, e);
        }
    }
}