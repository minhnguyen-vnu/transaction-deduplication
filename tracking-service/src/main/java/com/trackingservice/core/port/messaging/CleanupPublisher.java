package com.trackingservice.core.port.messaging;

import com.trackingservice.core.domain.dto.KeyCleanupRequested;

public interface CleanupPublisher {
    void publish(KeyCleanupRequested event);
}
