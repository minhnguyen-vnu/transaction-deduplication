package com.testingservice.domain.port.inbound;

import com.testingservice.domain.model.RunRequest;
import com.testingservice.domain.model.RunResult;

public interface RunScenarioUseCase {
    RunResult run(RunRequest request) throws InterruptedException;
}
