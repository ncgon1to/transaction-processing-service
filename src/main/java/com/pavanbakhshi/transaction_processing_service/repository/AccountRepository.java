package com.pavanbakhshi.transaction_processing_service.repository;

import com.pavanbakhshi.transaction_processing_service.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends  JpaRepository<Account, UUID> {
}