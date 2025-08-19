package com.example.order_service.infrastructure.config.datasource;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // để auto createdAt/updatedAt
public class DataSourceConfig {
    // info db sẽ lấy từ application.yml
}