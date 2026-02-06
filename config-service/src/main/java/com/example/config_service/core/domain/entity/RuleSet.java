package com.example.config_service.core.domain.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "rule_set",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"rule_name", "version"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Lob
    @Column(name = "drl", columnDefinition = "LONGTEXT", nullable = false)
    private String drl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "rule_set_globals",
            joinColumns = @JoinColumn(name = "rule_set_id")
    )
    @MapKeyColumn(name = "global_key")
    @Column(name = "bean_alias")
    private Map<String, String> globals;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "rule_set_rules",
            joinColumns = @JoinColumn(name = "rule_set_id")
    )
    @MapKeyColumn(name = "rule_index")
    @Column(name = "rule_name")
    private Map<Integer, String> ruleMap;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}