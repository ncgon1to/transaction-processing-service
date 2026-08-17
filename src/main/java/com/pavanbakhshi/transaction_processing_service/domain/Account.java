package com.pavanbakhshi.transaction_processing_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
public class Account {

    @Id
    private UUID id;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "overdraft_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal overdraftLimit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {
        // Required by JPA. Not for application use.
    }

    public Account(String customerName, String currency, BigDecimal overdraftLimit) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.overdraftLimit = overdraftLimit;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
    }
}