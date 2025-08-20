package com.testing_service.adapter.outbound.stub;

import com.testing_service.domain.model.Transaction;
import com.testing_service.domain.port.outbound.BPort;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class StubBClient implements BPort {

    @Override
    public String execute(Transaction tx) {
        try { Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200)); } catch (InterruptedException ignored) {}
        return ThreadLocalRandom.current().nextDouble() < 0.85 ? "SUCCESS" : "TIMEOUT";
    }
}
