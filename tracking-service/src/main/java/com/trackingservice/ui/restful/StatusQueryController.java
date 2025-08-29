package com.trackingservice.ui.restful;

import com.trackingservice.core.port.store.StatusStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusQueryController {
    private final StatusStore store;

    @GetMapping("/request/{requestId}")
    public Object byRequestId(@PathVariable String requestId) {
        return store.findByRequestId(requestId).orElse(null);
    }

    @GetMapping("/key/{idempotentKey}")
    public Object byKey(@PathVariable String idempotentKey) {
        return store.findByIdempotentKey(idempotentKey).orElse(null);
    }
}
