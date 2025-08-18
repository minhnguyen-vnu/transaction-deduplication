package com.testing_service.domain.port.outbound;

import com.testing_service.domain.model.Transaction;

public interface DedupPort {
    boolean isAllowed(Transaction tx);
}
