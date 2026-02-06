package com.example.config_service.infrastructure.store.repository;

import com.example.config_service.core.domain.entity.RuleSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, Long> {

    Optional<RuleSet> findByRuleNameAndIsActiveTrue(String ruleName);

    Optional<RuleSet> findByRuleNameAndVersion(String ruleName, Integer version);

    Optional<RuleSet> findTopByRuleNameAndIsActiveTrueOrderByVersionDesc(String ruleName);

    @Query("select distinct r.ruleName from RuleSet r")
    List<String> findDistinctRuleNames();

    List<RuleSet> findTopNByRuleNameOrderByVersionDesc(String ruleName, Pageable pageable);

    default List<RuleSet> findTopNByRuleNameOrderByVersionDesc(String ruleName, int limit) {
        return findTopNByRuleNameOrderByVersionDesc(ruleName, PageRequest.of(0, limit));
    }
}