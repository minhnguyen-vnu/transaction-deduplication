package com.example.config_service.core.service;

import java.nio.file.Path;

public interface RuleBuilder {
    void buildAndSaveRules();
    void buildFromFile(Path file);
}

