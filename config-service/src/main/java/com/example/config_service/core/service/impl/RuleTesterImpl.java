package com.example.config_service.core.service.impl;

import com.example.config_service.core.domain.dto.RuleBuildResult;
import com.example.config_service.core.domain.entity.RuleSet;
import com.example.config_service.core.service.RuleTester;
import com.example.config_service.infrastructure.cache.redis.RedisCacheTemplate;
import com.example.config_service.infrastructure.store.repository.RuleSetRepository;
import com.example.config_service.kernels.utils.RuleParserUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleTesterImpl implements RuleTester {

    private final RuleSetRepository ruleStore;
    private final RedisCacheTemplate redisCache;
    private final ApplicationContext applicationContext;
    private final Cache<String, KieBase> kieBaseCache;

    // ================== Core reusable methods ==================

    /** Build KieSession từ DRL */
    private KieSession buildSession(String drl) {
        long start = System.nanoTime();

        KieHelper helper = new KieHelper();
        helper.addContent(drl, "dynamic-rule.drl");
        KieBase kieBase = helper.build();
        long kieBaseEnd = System.nanoTime();
        long kieBaseDurationMs = (kieBaseEnd - start) / 1_000_000; // ms
        log.info("⏱️ kieBase took {} ms", kieBaseDurationMs);
        KieSession kieSession = kieBase.newKieSession();

        // Default global
        kieSession.setGlobal("passed", new AtomicBoolean(true));

        long end = System.nanoTime();
        long durationMs = (end - start) / 1_000_000; // ms
        log.info("⏱️ buildSession took {} ms", durationMs);

        return kieSession;
    }

    public KieSession buildCachedSession(String ruleKey, String drl) {
        long start = System.nanoTime();

        // Lấy từ cache
        KieBase kieBase = kieBaseCache.get(ruleKey, key -> {
            log.info("⚙️ Building new KieBase for ruleKey={}", key);
            KieHelper helper = new KieHelper();
            helper.addContent(drl, "dynamic-rule.drl");
            return helper.build();
        });

        KieSession kieSession = kieBase.newKieSession();
        kieSession.setGlobal("passed", new AtomicBoolean(true));

        long end = System.nanoTime();
        long durationMs = (end - start) / 1_000_000; // ms
        log.info("⏱️ buildSession took {} ms", durationMs);

        return kieSession;
    }

    /** Đặt globals từ DRL vào KieSession bằng Spring beans */
    private RuleBuildResult setGlobals(KieSession kieSession, String drl) {
        Map<String, String> globals = RuleParserUtils.extractGlobals(drl);

        for (Map.Entry<String, String> entry : globals.entrySet()) {
            String varName = entry.getKey();
            String className = entry.getValue();

            try {
                Class<?> clazz = Class.forName(className);

                Map<String, ?> candidates = applicationContext.getBeansOfType(clazz);
                if (candidates.isEmpty()) {
                    String msg = String.format("❌ No Spring bean found for global %s of type %s", varName, className);
                    log.error(msg);
                    return RuleBuildResult.fail(msg);
                }

                Object beanToUse = candidates.values().iterator().next(); // chọn bean đầu tiên
                if (candidates.size() > 1) {
                    log.warn("⚠️ Multiple beans found for global {} of type {}. Using bean: {}",
                            varName, className, beanToUse.getClass().getName());
                }

                kieSession.setGlobal(varName, beanToUse);
                log.info("✅ Set global '{}' with bean type {}", varName, beanToUse.getClass().getName());

            } catch (ClassNotFoundException e) {
                String msg = String.format("❌ Class not found for global %s -> %s", varName, className);
                log.error(msg, e);
                return RuleBuildResult.fail(msg);
            } catch (Exception e) {
                String msg = String.format("❌ Error resolving bean for global %s of type %s", varName, className);
                log.error(msg, e);
                return RuleBuildResult.fail(msg);
            }
        }

        return RuleBuildResult.success(null);
    }

    /** Insert facts vào session */
    private void insertFacts(KieSession kieSession, Object... facts) {
        if (facts != null) {
            for (Object fact : facts) {
                kieSession.insert(fact);
            }
        }
    }

    // ================== Public APIs ==================

    public RuleBuildResult testBuildFromDrl(String drl) {
        try (KieSession kieSession = buildSession(drl)) {
            RuleBuildResult globalsResult = setGlobals(kieSession, drl);
            if (!globalsResult.isSuccess()) return globalsResult;

            log.info("✅ Rule successfully built");
            return RuleBuildResult.success("Rule compiled OK");
        } catch (Exception e) {
            log.error("❌ Error while building DRL", e);
            return RuleBuildResult.fail(e.getMessage());
        }
    }

    public RuleBuildResult fireFromDrl(String drl, String ruleName, Object... facts) {
        try (KieSession kieSession = buildCachedSession(ruleName, drl)) {

            Map<Integer, String> ruleMap = RuleParserUtils.extractRuleMap(drl);

            RuleBuildResult globalsResult = setGlobals(kieSession, drl);
            if (!globalsResult.isSuccess()) return globalsResult;

            insertFacts(kieSession, facts);

            int fired = kieSession.fireAllRules();
            log.info("🔥 Fired {} rules", fired);

            AtomicBoolean isPassed = (AtomicBoolean) kieSession.getGlobal("passed");
            if (isPassed.get()) {
                return RuleBuildResult.success("Fired " + fired + " rules");
            } else {
                String failedRule = ruleMap.get(fired);
                return RuleBuildResult.fail(
                        "Failed at rule: " + (failedRule != null ? failedRule : ("#" + fired))
                );
            }
        } catch (Exception e) {
            log.error("❌ Error while firing rules from DRL", e);
            return RuleBuildResult.fail(e.getMessage());
        }
    }

    public RuleBuildResult testFromDb(Long ruleId) {
        RuleSet ruleSet = ruleStore.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));
        return testBuildFromDrl(ruleSet.getDrl());
    }

    public RuleBuildResult testFromRedis(String key) {
        try {
            Optional<RuleSet> cached = redisCache.get(key);
            if (cached.isEmpty()) {
                return RuleBuildResult.fail("Rule not found in Redis: " + key);
            }
            return testBuildFromDrl(cached.get().getDrl());
        } catch (Exception e) {
            return RuleBuildResult.fail("Error retrieving rule from Redis: " + e.getMessage());
        }
    }
}