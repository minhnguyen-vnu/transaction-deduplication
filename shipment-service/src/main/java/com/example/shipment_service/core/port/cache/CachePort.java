package com.example.shipment_service.core.port.cache;

public interface CachePort {
    void put(String key, Object value);
    Object get(String key);
    void evict(String key);
}