package com.testingservice.adapater.inbound.rest;

import com.testingservice.domain.model.RunRequest;
import com.testingservice.domain.model.RunResult;
import com.testingservice.domain.port.inbound.RunScenarioUseCase;
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
