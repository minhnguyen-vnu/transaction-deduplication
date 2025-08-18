package com.testing_service.adapter.outbound.stub;

import com.testing_service.config.TestingProperties;
import com.testing_service.domain.model.Transaction;
import com.testing_service.domain.port.outbound.DedupPort;
import com.testing_service.domain.service.KeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StubDedupClient implements DedupPort {
    private final InMemoryDedupRepository repo;
    private final TestingProperties props;

    @Override
    public boolean isAllowed(Transaction tx) {
        repo.setTtlMinutes(props.getDedup().getTtlMinutes());
        var canonical = KeyUtil.canonicalForDedup(tx.getAmount(), tx.getUserID(), tx.getPhone(), tx.getIP());
        var key = KeyUtil.sha256Hex(canonical);
        return repo.reserve(key);
    }
}
