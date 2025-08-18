package com.testingservice;

import com.testingservice.config.TestingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TestingProperties.class)
public class TestingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestingServiceApplication.class, args);
    }

}
