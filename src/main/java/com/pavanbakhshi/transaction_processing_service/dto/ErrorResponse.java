package com.pavanbakhshi.transaction_processing_service.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, Instant.now(), path, List.of());
    }

    public static ErrorResponse of(String code, String message, String path,
                                   List<FieldError> fieldErrors) {
        return new ErrorResponse(code, message, Instant.now(), path, fieldErrors);
    }
}