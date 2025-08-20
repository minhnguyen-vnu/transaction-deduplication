package com.testing_service.domain.port.inbound;

import com.testing_service.domain.model.RunRequest;
import com.testing_service.domain.model.RunResult;

public interface RunScenarioUseCase {
    RunResult run(RunRequest request) throws InterruptedException;
}
