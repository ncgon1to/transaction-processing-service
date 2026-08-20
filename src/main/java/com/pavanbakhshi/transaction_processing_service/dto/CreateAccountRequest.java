package com.pavanbakhshi.transaction_processing_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank(message = "customerName is required")
        @Size(max = 200, message = "customerName must be at most 200 characters")
        String customerName,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be three uppercase letters")
        String currency,

        @NotNull(message = "overdraftLimit is required")
        @DecimalMin(value = "0.0", message = "overdraftLimit must not be negative")
        @Digits(integer = 15, fraction = 4, message = "overdraftLimit must have at most 4 decimal places")
        BigDecimal overdraftLimit
) {
}