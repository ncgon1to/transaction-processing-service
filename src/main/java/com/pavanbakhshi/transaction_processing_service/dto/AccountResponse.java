package com.pavanbakhshi.transaction_processing_service.dto;

import com.pavanbakhshi.transaction_processing_service.domain.Account;
import com.pavanbakhshi.transaction_processing_service.domain.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String customerName,
        String currency,
        AccountStatus status,
        BigDecimal overdraftLimit,
        BigDecimal balance,
        Instant createdAt
) {
    public static AccountResponse from(Account account, BigDecimal balance) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerName(),
                account.getCurrency(),
                account.getStatus(),
                account.getOverdraftLimit(),
                balance,
                account.getCreatedAt()
        );
    }
}