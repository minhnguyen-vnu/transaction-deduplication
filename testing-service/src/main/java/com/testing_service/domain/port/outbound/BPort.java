package com.testing_service.domain.port.outbound;

import com.testing_service.domain.model.Transaction;

public interface BPort {
    String execute(Transaction tx);
}
