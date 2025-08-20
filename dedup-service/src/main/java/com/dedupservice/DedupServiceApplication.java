package com.dedupservice;

import com.dedupservice.infrastructure.config.DedupProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DedupProperties.class)
public class DedupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DedupServiceApplication.class, args);
    }

}
