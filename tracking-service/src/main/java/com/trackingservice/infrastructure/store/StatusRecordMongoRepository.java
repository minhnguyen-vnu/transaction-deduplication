package com.trackingservice.infrastructure.store;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StatusRecordMongoRepository extends MongoRepository<StatusRecordDocument, String> {
    Optional<StatusRecordDocument> findByIdempotentKey(String key);
    Optional<StatusRecordDocument> findByRequestId(String requestId);
}
