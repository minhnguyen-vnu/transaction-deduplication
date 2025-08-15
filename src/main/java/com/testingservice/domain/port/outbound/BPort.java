package com.testingservice.domain.port.outbound;

import com.testingservice.domain.model.Transaction;

public interface BPort {
    String execute(Transaction tx);
}
