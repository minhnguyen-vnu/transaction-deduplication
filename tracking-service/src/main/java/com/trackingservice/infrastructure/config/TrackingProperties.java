package com.trackingservice.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tracking")
public class TrackingProperties {
    private Topics topics = new Topics();

    @Data
    public static class Topics {
        private String inbound = "txn.status.in";
        private String cleanup = "dedup.cleanup.request";
    }
}
