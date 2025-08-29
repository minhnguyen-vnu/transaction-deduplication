package com.trackingservice.core.domain.constant;

public enum TxnStatus {
    PENDING, SUCCEEDED, FAILED;

    public static boolean isTerminal(TxnStatus s) {
        return s == SUCCEEDED || s == FAILED;
    }
}
