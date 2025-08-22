package com.dedupservice.ui.restful;

import com.dedupservice.core.domain.dto.DedupCheckRequest;
import com.dedupservice.core.domain.dto.DedupResult;
import com.dedupservice.core.service.DedupUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DedupController {
    private final DedupUseCase useCase;
    @PostMapping(value = "/dedup-check",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DedupResult dedup(@Valid @RequestBody DedupCheckRequest req) {
        return useCase.check(req);
    }
}
