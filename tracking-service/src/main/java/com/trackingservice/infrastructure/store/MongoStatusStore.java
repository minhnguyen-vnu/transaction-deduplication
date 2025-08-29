package com.trackingservice.infrastructure.store;

import com.trackingservice.core.domain.entity.StatusRecord;
import com.trackingservice.core.port.store.StatusStore;
import com.trackingservice.kernel.utils.StatusRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoStatusStore implements StatusStore {
    private final StatusRecordMongoRepository repo;

    @Override
    public StatusRecord save(StatusRecord record) {
        return StatusRecordMapper.toDomain(repo.save(StatusRecordMapper.toDoc(record)));
    }

    @Override
    public StatusRecord insert(StatusRecord record) {
        return StatusRecordMapper.toDomain(repo.insert(StatusRecordMapper.toDoc(record)));
    }

    @Override
    public Optional<StatusRecord> findByIdempotentKey(String key) {
        return repo.findByIdempotentKey(key).map(StatusRecordMapper::toDomain);
    }

    @Override
    public Optional<StatusRecord> findByRequestId(String requestId) {
        return repo.findByRequestId(requestId).map(StatusRecordMapper::toDomain);
    }
}
