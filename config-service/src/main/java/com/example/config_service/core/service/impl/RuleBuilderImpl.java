package com.example.config_service.core.service.impl;


import com.example.config_service.core.domain.dto.RuleBuildResult;
import com.example.config_service.core.domain.entity.RuleSet;
import com.example.config_service.core.service.RuleTester;
import com.example.config_service.core.service.RuleWatcher;
import com.example.config_service.infrastructure.cache.redis.RedisCacheTemplate;
import com.example.config_service.infrastructure.config.RuleProperties;
import com.example.config_service.infrastructure.store.repository.RuleSetRepository;
import com.example.config_service.kernels.utils.RuleParserUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBuilderImpl {

    private final RuleTester tester;
    private final RuleSetRepository ruleSetRepository;
    private final RedisCacheTemplate redisCache;
    private final RuleProperties ruleProperties;
    private final RuleWatcher ruleWatcher;
    private ExecutorService executor;


    private static final String RULE_FOLDER = "src/main/resources";
    @PostConstruct
    public void initWatcher() throws Exception {
        Path ruleFolder = Paths.get(RULE_FOLDER, ruleProperties.getFolder());

        if (!Files.exists(ruleFolder) || !Files.isDirectory(ruleFolder)) {
            log.warn("Rule folder {} not found or not a directory, watcher disabled",
                    ruleFolder.toAbsolutePath());
            return;
        }

        initRules(ruleFolder);

        // Tạo thread riêng
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                ruleWatcher.watch(ruleFolder, ruleProperties.getDebounceTimeMs(), this::buildAndSave);
            } catch (Exception e) {
                log.error("Watcher error", e);
            }
        });
    }


    @PreDestroy
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            log.info("Watcher executor shut down.");
        }
    }

    private void initRules(Path ruleFolder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(ruleFolder, "*.drl")) {
            for (Path drlFile : stream) {
                processRuleFile(drlFile);
            }
        } catch (Exception e) {
            log.error("Error scanning rule folder {}", ruleFolder, e);
        }
    }

    private void processRuleFile(Path drlFile) {
        try {
            String drl = Files.readString(drlFile);
            String ruleName = extractRuleName(drlFile);

            log.info("⏳ Initializing rule file {} -> ruleName={}", drlFile, ruleName);

            // Bước 1: kiểm tra build
            if (!testRuleBuild(drlFile, drl)) {
                log.warn("❌ Skip rule {} due to build failure", ruleName);
                return;
            }

            // Bước 2: kiểm tra DB + Redis
            boolean sameAsDb = isSameAsActive(ruleName, drl);
            boolean redisHasKey = redisCache.contains(buildLatestKey(ruleName));

            if (sameAsDb && redisHasKey) {
                log.info("✅ Rule {} unchanged and already cached, skip", ruleName);
                return;
            }

            // Bước 3: tạo rule mới nếu khác DB, hoặc chỉ cache lại nếu Redis thiếu
            RuleSet newRule;
            if (!sameAsDb) {
                // tạo version mới
                newRule = createNextRuleSet(ruleName, drl);
                deactivateOldRule(ruleName);
                persistAndCache(newRule);
            } else {
                // DB có rồi, lấy bản active hiện tại để cache lại
                newRule = ruleSetRepository.findByRuleNameAndIsActiveTrue(ruleName)
                        .orElseThrow(() -> new IllegalStateException("Active rule not found for " + ruleName));

                redisCache.put(buildRuleKey(newRule.getRuleName(), newRule.getVersion()), newRule);
                redisCache.put(buildLatestKey(ruleName), newRule);
                redisCache.putForever(buildCurrentVersionKey(newRule.getRuleName()), newRule.getVersion());
                log.info("♻️ Rule {} already active in DB but missing in Redis, recaching", ruleName);
            }

//        tester.fireFromDrl(drl);

        } catch (Exception e) {
            log.error("Error initializing rule from file {}", drlFile, e);
        }
    }

    private void buildAndSave(Path drlFile) {
        try {
            if (!Files.exists(drlFile)) {
                log.warn("Changed file {} no longer exists, skip", drlFile);
                return;
            }

            String drl = Files.readString(drlFile);
            String ruleName = extractRuleName(drlFile);

            log.info("Detected rule change in file {} -> ruleName={}", drlFile, ruleName);

            if (!testRuleBuild(drlFile, drl)) return;
            if (isSameAsActive(ruleName, drl)) {
                log.info("Rule {} unchanged, skip saving", ruleName);
                return;
            }

            RuleSet newRule = createNextRuleSet(ruleName, drl);
            deactivateOldRule(ruleName);
            persistAndCache(newRule);
//            tester.fireFromDrl(drl);

        } catch (Exception e) {
            log.error("Error while building rule from file {}", drlFile, e);
        }
    }

    private String extractRuleName(Path drlFile) {
        return drlFile.getFileName().toString().replace(".drl", "");
    }

    private boolean testRuleBuild(Path drlFile, String drl) {
        RuleBuildResult result = tester.testBuildFromDrl(drl);
        if (!result.isSuccess()) {
            log.error("Failed to build rule from {}: {}", drlFile, result.getMessage());
            return false;
        }
        return true;
    }

    private boolean isSameAsActive(String ruleName, String drl) {
        return ruleSetRepository.findByRuleNameAndIsActiveTrue(ruleName)
                .map(rs -> rs.getDrl().equals(drl))
                .orElse(false);
    }

    private RuleSet createNextRuleSet(String ruleName, String drl) {
        int nextVersion = ruleSetRepository.findByRuleNameAndIsActiveTrue(ruleName)
                .map(RuleSet::getVersion)
                .orElse(0) + 1;

        Map<String, String> globals = RuleParserUtils.extractGlobals(drl);
        Map<Integer, String> ruleMap = RuleParserUtils.extractRuleMap(drl);

        return RuleSet.builder()
                .ruleName(ruleName)
                .version(nextVersion)
                .drl(drl)
                .globals(globals)
                .ruleMap(ruleMap)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void deactivateOldRule(String ruleName) {
        ruleSetRepository.findByRuleNameAndIsActiveTrue(ruleName).ifPresent(old -> {
            old.setIsActive(false);
            ruleSetRepository.save(old);
        });
    }

    private void persistAndCache(RuleSet ruleSet) {
        ruleSetRepository.save(ruleSet);
        try {
            redisCache.put(buildRuleKey(ruleSet.getRuleName(), ruleSet.getVersion()), ruleSet);
            redisCache.put(buildLatestKey(ruleSet.getRuleName()), ruleSet);
            redisCache.putForever(buildCurrentVersionKey(ruleSet.getRuleName()), ruleSet.getVersion());
            log.info("✅ Cached rule {} v{} in Redis", ruleSet.getRuleName(), ruleSet.getVersion());
            log.info("Rule {} v{} saved to DB and cached in Redis",
                    ruleSet.getRuleName(), ruleSet.getVersion());
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache rule {} v{} into Redis. Continuing without cache.",
                    ruleSet.getRuleName(), ruleSet.getVersion(), e);
        }
    }

    private String buildRuleKey(String ruleName, Integer version) {
        return "rule:" + ruleName + ":" + version;
    }

    private String buildLatestKey(String ruleName) {
        return "rule:" + ruleName + ":latest";
    }

    private String buildCurrentVersionKey(String ruleName) {
        return String.format("rule:%s:currentVersion", ruleName);
    }
}

