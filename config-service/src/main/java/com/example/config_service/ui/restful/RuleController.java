package com.example.config_service.ui.restful;

import com.example.config_service.core.domain.dto.RuleGetDto;
import com.example.config_service.core.domain.dto.RuleValidateRequest;
import com.example.config_service.core.domain.dto.RuleValidateResponse;
import com.example.config_service.core.domain.entity.RuleSet;
import com.example.config_service.core.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping("/get")
    public ResponseEntity<RuleSet> getRule(
            @RequestParam(defaultValue = "general") String ruleName,
            @RequestParam(required = false) Integer version
    ) {
        Optional<RuleSet> rule = ruleService.getRule(ruleName, version);
        return rule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/validate")
    public ResponseEntity<RuleValidateResponse> validate(
            @Valid @RequestBody RuleValidateRequest request
    ) {
        RuleValidateResponse response = ruleService.validateRequest(request);
        return ResponseEntity.ok(response);
    }
}