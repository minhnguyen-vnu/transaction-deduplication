package com.example.order_service.core.port.client;

import com.example.order_service.core.domain.dto.dedup.DedupRequest;
import com.example.order_service.core.domain.dto.dedup.DedupResult;

public interface DedupClientPort {
    DedupResult checkDedup(DedupRequest request);
}
