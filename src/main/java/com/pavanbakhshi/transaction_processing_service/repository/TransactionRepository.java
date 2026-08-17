package com.pavanbakhshi.transaction_processing_service.repository;

import com.pavanbakhshi.transaction_processing_service.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}