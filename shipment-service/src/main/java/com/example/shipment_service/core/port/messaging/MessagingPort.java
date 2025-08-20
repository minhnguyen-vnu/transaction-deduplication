package com.example.shipment_service.core.port.messaging;

public interface MessagingPort {
    void publishOrderCreatedEvent(String message);
}