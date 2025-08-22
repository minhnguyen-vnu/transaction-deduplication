package com.dedupservice.core.port.store;

public interface IdempotencyStore {
    /**
     * Kiểm tra xem key đã tồn tại (duplicate) hay chưa
     */
    boolean isDuplicate(String idemKey);

    /**
     * Ghi nhận key đã xử lý
     */
    void recordProcessed(String idemKey);

    /**
     * Xóa key (nếu store hỗ trợ, vd cuckoo filter)
     */
    default void remove(String idemKey) {
        throw new UnsupportedOperationException("Remove not supported by this store");
    }
}