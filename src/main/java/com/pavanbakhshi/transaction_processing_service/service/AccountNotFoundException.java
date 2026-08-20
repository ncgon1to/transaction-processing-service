package com.pavanbakhshi.transaction_processing_service.service;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }
}