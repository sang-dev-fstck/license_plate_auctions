package com.auction.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, String> errors;

    public AppException(String message) {
        this(HttpStatus.BAD_REQUEST, message, null);
    }

    public AppException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public AppException(HttpStatus status, String message, Map<String, String> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public static AppException conflict(String field, String message) {
        return new AppException(
                HttpStatus.CONFLICT,
                message,
                Map.of(field, message)
        );
    }
}