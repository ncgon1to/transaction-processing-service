CREATE TABLE account (
    id              uuid            PRIMARY KEY,
    customer_name   varchar(200)    NOT NULL,
    currency        char(3)         NOT NULL,
    status          varchar(20)     NOT NULL DEFAULT 'ACTIVE',
    overdraft_limit numeric(19, 4)  NOT NULL DEFAULT 0,
    created_at      timestamptz     NOT NULL DEFAULT now(),

    CONSTRAINT account_currency_format
        CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT account_status_valid
        CHECK (status IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT account_overdraft_limit_non_negative
        CHECK (overdraft_limit >= 0)
);

CREATE TABLE transactions (
    id          uuid            PRIMARY KEY,
    account_id  uuid            NOT NULL,
    type        varchar(10)     NOT NULL,
    amount      numeric(19, 4)  NOT NULL,
    merchant    varchar(200),
    occurred_at timestamptz     NOT NULL,
    created_at  timestamptz     NOT NULL DEFAULT now(),

    CONSTRAINT transactions_account_fk
        FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT transactions_type_valid
        CHECK (type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT transactions_amount_positive
        CHECK (amount > 0)
);

CREATE TABLE request_record (
    id              uuid          PRIMARY KEY,
    idempotency_key varchar(255)  NOT NULL,
    request_hash    varchar(64)   NOT NULL,
    transaction_id  uuid,
    created_at      timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT request_record_key_unique
        UNIQUE (idempotency_key),
    CONSTRAINT request_record_transaction_fk
        FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

CREATE TABLE alert (
    id              uuid          PRIMARY KEY,
    transaction_id  uuid          NOT NULL,
    rule_code       varchar(50)   NOT NULL,
    reason          varchar(500)  NOT NULL,
    created_at      timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT alert_transaction_fk
        FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT alert_rule_code_valid
        CHECK (rule_code IN ('LARGE_AMOUNT', 'HIGH_FREQUENCY')),
    CONSTRAINT alert_transaction_rule_unique
        UNIQUE (transaction_id, rule_code)
);
