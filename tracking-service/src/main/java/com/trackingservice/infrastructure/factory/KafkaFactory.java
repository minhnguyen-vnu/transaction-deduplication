package com.trackingservice.infrastructure.factory;

import com.trackingservice.infrastructure.config.TrackingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TrackingProperties.class)
public class KafkaFactory {

}
