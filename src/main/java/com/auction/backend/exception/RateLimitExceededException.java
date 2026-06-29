package com.auction.backend.exception;

import lombok.Getter;

import java.time.Duration;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final Duration retryAfter;

    public RateLimitExceededException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

}
