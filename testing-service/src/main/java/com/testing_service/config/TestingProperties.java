package com.testing_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ts")
public class TestingProperties {
    private Dedup dedup = new Dedup();
    private B b = new B();

    @Data
    public static class Dedup {
        private String mode = "stub";        // stub | http
        private String baseUrl;
        private String path = "/api/dedup-check";
        private long ttlMinutes = 10;
    }
    @Data public static class B {
        private String mode = "stub";
        private String baseUrl;
        private String path = "/do-transaction";
    }
}
