package com.trackingservice.core.port.store;

import com.trackingservice.core.domain.entity.StatusRecord;

import java.util.Optional;

public interface StatusStore {
    StatusRecord save(StatusRecord record);
    StatusRecord insert(StatusRecord record);
    Optional<StatusRecord> findByIdempotentKey(String key);
    Optional<StatusRecord> findByRequestId(String requestId);
}
