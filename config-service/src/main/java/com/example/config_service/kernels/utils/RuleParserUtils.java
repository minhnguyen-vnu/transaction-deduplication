package com.example.config_service.kernels.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class RuleParserUtils {

    private static final Pattern GLOBAL_PATTERN =
            Pattern.compile("global\\s+([\\w\\.]+)\\s+(\\w+)\\s*;");

    private static final Pattern RULE_PATTERN =
            Pattern.compile("^\\s*rule\\s+\"([^\"]+)\"");

    public Map<String, String> extractGlobals(String drl) {
        Map<String, String> globals = new HashMap<>();
        String[] lines = drl.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            Matcher matcher = GLOBAL_PATTERN.matcher(line);
            if (matcher.find()) {
                String className = matcher.group(1); // full class name
                String varName   = matcher.group(2); // variable alias

                if ("passed".equals(varName)) {
                    continue;
                }

                globals.put(varName, className); // giữ nguyên full class name
                log.debug("Found global: {} {}", className, varName);
            }
        }
        return globals;
    }

    public Map<Integer, String> extractRuleMap(String drl) {
        Map<Integer, String> ruleMap = new LinkedHashMap<>();
        String[] lines = drl.split("\\r?\\n");
        int index = 1;

        for (String line : lines) {
            Matcher matcher = RULE_PATTERN.matcher(line);
            if (matcher.find()) {
                String ruleName = matcher.group(1);
                ruleMap.put(index++, ruleName);
                log.debug("Found rule: {} (index={})", ruleName, index - 1);
            }
        }

        return ruleMap;
    }
}
