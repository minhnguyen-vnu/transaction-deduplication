package com.dedupservice.core.port.store;

public interface DedupStore {
    boolean reserve(String key, long ttlSeconds);
    default boolean exists(String key){ return false; }
    default void release(String key){}
}
