package com.trackingservice.core.domain.constant.error;

public class PublishFailedException extends RuntimeException {
    public PublishFailedException(String message, Throwable cause) { super(message, cause); }
    public PublishFailedException(String message) { super(message); }
}
