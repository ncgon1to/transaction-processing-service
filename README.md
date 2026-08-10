# transaction-processing-service

A REST service for customer accounts and financial transactions, built to demonstrate production-shaped backend engineering in JAVA 21 and Spring Boot. 

## Problem

Systems that move money cannot assume a request arrives exactly once. Networks drop, clients retry and a naive sercive will process the same debit twice. They also cannot treat a balance as a value to be overwritten, because an overwritten balance can silently disagree with the history that produced it. 

This service addresses both: transaction submission is idempotent, and the
transaction ledger is append-only with balance derived from it rather than
stored alongside it. It additionally flags unusual activity using transparent,
explainable rules.

## Users

- **Internal service clients** submitting transactions on behalf of customers, which may retry any request without risking duplication 
- **Operations staff** querying an account's transaction history with filters
  and pagination, and reviewing flagged transactions with a stated reason.

## Non-goals

This is a learning system, not a bank. Deliberately out of scope:

- Authentication and authorisation. There is no user identity model.
- Real payment rails, settlement, clearing or interbank messaging.
- Double-entry bookkeeping. Transactions belong to one account; there is no
  contra account.
- Multi-currency conversion. An account has one currency and transactions must
  match it.
- Regulatory compliance, KYC, or sanctions screening.
- Machine-learning fraud detection. Alert rules are deterministic by design so
  that every flag can be explained.

## Acceptance criteria

**Accounts**
- `POST /accounts` creates an account with a customer name, three-letter
  currency code and an overdraft limit; returns 201 and the created resource.
- `GET /accounts/{id}` returns the account with its current derived balance,
  or 404 if it does not exist.


**Transaction submission**
- `POST /accounts/{id}/transactions` accepts a type (CREDIT or DEBIT), a
  positive amount, a merchant and an occurrence timestamp.
- Every submission requires an `Idempotency-Key` header.
- A repeated key with an identical canonical request body returns the original
  result, and creates no second transaction.
- A repeated key with a different request body returns 409 Conflict.
- A debit is rejected with 422 if it would take the balance below the negative
  of the account's overdraft limit.
- The balance check, idempotency record and ledger insert occur in a single
  database transaction. Concurrent debits cannot together breach the overdraft
  limit. 

**Queries**
- `GET /accounts/{id}/transactions` supports `page`, `size`, `sort`,
  `dateFrom`, `dateTo` and `type`.
- Balance is derived from the transaction ledger. Transaction rows are never
  updated or deleted.

**Alerts**
- `LARGE_AMOUNT` fires when a transaction amount exceeds £5,000.
- `HIGH_FREQUENCY` fires when an account records more than 10 transactions
  within one hour.
- Alerts do not block a transaction. Each stores a rule code and a
  human-readable reason.
- At most one alert exists per transaction and rule combination.
- `GET /accounts/{id}/alerts` lists alerts for an account.

**Errors and operations**
- All errors return one documented JSON schema: code, message, timestamp, path
  and field errors.
- Stack traces and credentials are never exposed in a response.
- Health and readiness endpoints are available via Actuator.

## API endpoints

| Method | Path                            | Purpose                          |
|--------|---------------------------------|----------------------------------|
| POST   | `/accounts`                     | Create an account                |
| GET    | `/accounts/{id}`                | Fetch an account and its balance |
| POST   | `/accounts/{id}/transactions`   | Submit a transaction (idempotent)|
| GET    | `/accounts/{id}/transactions`   | List transactions, filtered      |
| GET    | `/accounts/{id}/alerts`         | List alerts for an account       |
| GET    | `/actuator/health`              | Liveness and readiness           |
