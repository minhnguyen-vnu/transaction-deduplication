package com.example.config_service.core.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "service_config",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"service_name", "config_key"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "config_key", nullable = false, length = 255)
    private String configKey;

    @Lob
    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}