package com.example.order_service.core.port.messaging;

public interface MessagingPort {
    void publishOrderCreatedEvent(String message);
}