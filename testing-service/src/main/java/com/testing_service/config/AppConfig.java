package com.testing_service.config;

import com.testing_service.adapter.outbound.http.HttpBClient;
import com.testing_service.adapter.outbound.http.HttpDedupClient;
import com.testing_service.adapter.outbound.stub.StubBClient;
import com.testing_service.adapter.outbound.stub.StubDedupClient;
import com.testing_service.domain.port.outbound.BPort;
import com.testing_service.domain.port.outbound.DedupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final TestingProperties props;

    @Bean
    RestTemplate restTemplate() { return new RestTemplate(); }


    @Bean @Primary
    public DedupPort dedupPort(HttpDedupClient http, StubDedupClient stub) {
        return "http".equalsIgnoreCase(props.getDedup().getMode()) ? http : stub;
    }

    @Bean @Primary
    public BPort bPort(HttpBClient http, StubBClient stub) {
        return "http".equalsIgnoreCase(props.getB().getMode()) ? http : stub;
    }
}
