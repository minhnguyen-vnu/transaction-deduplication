package com.example.config_service.core.service;

import java.io.IOException;
import java.nio.file.Path;

public interface RuleWatcher {

    /**
     * Start watching a folder for .drl rule changes.
     * @param ruleFolder path to the rule folder
     * @param debounceTimeMs debounce time in milliseconds
     * @param onRuleChanged callback khi có file rule thay đổi
     */
    void watch(Path ruleFolder, long debounceTimeMs, RuleChangeCallback onRuleChanged) throws IOException;

    @FunctionalInterface
    interface RuleChangeCallback {
        void onChanged(Path drlFile);
    }
}
