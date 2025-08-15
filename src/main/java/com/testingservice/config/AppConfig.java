package com.testingservice.config;

import com.testingservice.adapater.outbound.http.HttpBClient;
import com.testingservice.adapater.outbound.http.HttpDedupClient;
import com.testingservice.adapater.outbound.stub.StubBClient;
import com.testingservice.adapater.outbound.stub.StubDedupClient;
import com.testingservice.domain.port.outbound.BPort;
import com.testingservice.domain.port.outbound.DedupPort;
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
