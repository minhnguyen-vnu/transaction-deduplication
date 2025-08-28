package com.dedupservice.core.port.messaging;

public interface MessagingPort {
    void publishOrderCreatedEvent(String message);
}