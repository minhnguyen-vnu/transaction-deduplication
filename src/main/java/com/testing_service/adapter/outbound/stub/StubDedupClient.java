package com.testingservice.adapater.outbound.stub;

import com.testingservice.config.TestingProperties;
import com.testingservice.domain.model.Transaction;
import com.testingservice.domain.port.outbound.DedupPort;
import com.testingservice.domain.service.KeyUtil;
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
