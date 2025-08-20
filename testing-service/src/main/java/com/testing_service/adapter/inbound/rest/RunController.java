package com.testing_service.adapter.inbound.rest;

import com.testing_service.domain.model.RunRequest;
import com.testing_service.domain.model.RunResult;
import com.testing_service.domain.port.inbound.RunScenarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RunController {
    private final RunScenarioUseCase useCase;

    @PostMapping("/run")
    public RunResult run(@RequestBody(required=false) RunRequest req) throws InterruptedException {
        return useCase.run(req == null ? new RunRequest() : req);
    }
}
