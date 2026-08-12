-- V2: indexes supporting the query patterns in the acceptance criteria.

CREATE INDEX idx_transactions_account_occurred_at
    ON transactions (account_id, occurred_at DESC);

