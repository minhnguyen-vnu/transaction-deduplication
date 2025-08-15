package com.testingservice.domain.port.outbound;

import com.testingservice.domain.model.Transaction;

public interface DedupPort {
    boolean isAllowed(Transaction tx);
}
