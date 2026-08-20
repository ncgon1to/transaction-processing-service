package com.pavanbakhshi.transaction_processing_service.service;

import com.pavanbakhshi.transaction_processing_service.domain.Account;
import com.pavanbakhshi.transaction_processing_service.dto.AccountResponse;
import com.pavanbakhshi.transaction_processing_service.dto.CreateAccountRequest;
import com.pavanbakhshi.transaction_processing_service.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account(
                request.customerName(),
                request.currency(),
                request.overdraftLimit()
        );
        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        BigDecimal balance = calculateBalance(id);
        return AccountResponse.from(account, balance);
    }

    private BigDecimal calculateBalance(UUID accountId) {
        // Placeholder until the transaction endpoints exist. A new account has
        // no ledger entries, so its balance is zero by definition.
        return BigDecimal.ZERO;
    }

    
}