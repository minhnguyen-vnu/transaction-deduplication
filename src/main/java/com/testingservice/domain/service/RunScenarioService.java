package com.testingservice.domain.service;

import com.testingservice.domain.model.RunRequest;
import com.testingservice.domain.model.RunResult;
import com.testingservice.domain.model.Transaction;
import com.testingservice.domain.port.inbound.RunScenarioUseCase;
import com.testingservice.domain.port.outbound.BPort;
import com.testingservice.domain.port.outbound.DedupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RunScenarioService implements RunScenarioUseCase {

    private final DedupPort dedup;
    private final BPort b;


    @Override
    public RunResult run(RunRequest cfg) throws InterruptedException {
        int threads = cfg.getThreads();
        int perThread = cfg.getPerThread();
        long pace = cfg.getPaceMillis();

        var pool = Executors.newFixedThreadPool(threads);
        var latch = new CountDownLatch(threads);
        var totals = new Totals();
        AtomicInteger txIdGen = new AtomicInteger(1000);

        for (int t=0; t<threads; t++) {
            final int threadNo = t;
            pool.submit(() -> {
                try {
                    for (int i=1; i<=perThread; i++) {
                        int txId = 1000 + i + (threadNo % 3);
                        double amount = 1000.00 * i;    // SAME across threads for same i → duplicates
                        int userId = 100 + i;
                        String phone = "0900" + i;
                        String ip = "10.0.0." + i;

                        var tx = Transaction.builder()
                                .transactionID(txId).amount(amount).userID(userId)
                                .phone(phone).IP(ip).timestamp(Instant.now()).build();

                        totals.incAttempted();
                        boolean allow = dedup.isAllowed(tx);
                        if (allow) {
                            totals.incAllowed();
                            String s = b.execute(tx);
                            if ("SUCCESS".equals(s)) totals.incBSuccess();
                            else if ("TIMEOUT".equals(s)) totals.incBTimeout();
                        } else {
                            totals.incDuplicate();
                        }
                        Thread.sleep(pace);
                    }
                } catch (InterruptedException ignored) {
                } finally { latch.countDown(); }
            });
        }
        latch.await(2, TimeUnit.MINUTES);
        pool.shutdownNow();
        return totals.toResult();
    }

    private static class Totals {
        int attempted, allowed, duplicate, bSuccess, bTimeout;
        synchronized void incAttempted(){attempted++;}
        synchronized void incAllowed(){allowed++;}
        synchronized void incDuplicate(){duplicate++;}
        synchronized void incBSuccess(){bSuccess++;}
        synchronized void incBTimeout(){bTimeout++;}
        RunResult toResult(){ return RunResult.builder()
                .attempted(attempted).allowed(allowed).duplicate(duplicate)
                .bSuccess(bSuccess).bTimeout(bTimeout).build(); }
    }
}
