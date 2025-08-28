package com.example.order_service.infrastructure.client;

import com.example.order_service.core.domain.dto.dedup.DedupRequest;
import com.example.order_service.core.domain.dto.dedup.DedupResult;
import com.example.order_service.core.port.client.DedupClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DedupClientAdapter implements DedupClientPort {
    private final RestTemplate restTemplate;

    @Value("${dedup.base-url}")
    private String dedupBaseUrl;

    @Override
    public DedupResult checkDedup(DedupRequest request) {
        String url = dedupBaseUrl + "/api/dedup-check";
        return restTemplate.postForObject(url, request, DedupResult.class);
    }
}
