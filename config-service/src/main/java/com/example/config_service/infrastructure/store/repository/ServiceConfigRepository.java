package com.example.config_service.infrastructure.store.repository;


import com.example.config_service.core.domain.entity.ServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceConfigRepository extends JpaRepository<ServiceConfig, Long> {

    Optional<ServiceConfig> findByServiceNameAndConfigKey(String serviceName, String configKey);

    List<ServiceConfig> findByServiceName(String serviceName);
}
