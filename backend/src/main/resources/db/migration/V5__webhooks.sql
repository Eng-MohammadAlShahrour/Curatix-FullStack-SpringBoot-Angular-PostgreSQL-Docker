CREATE TABLE webhooks (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    url           VARCHAR(500) NOT NULL,
    event_types   VARCHAR(200) NOT NULL,
    secret        VARCHAR(100),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhooks_active ON webhooks(active);

CREATE TABLE webhook_deliveries (
    id              BIGSERIAL PRIMARY KEY,
    webhook_id      BIGINT       NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    event_type      VARCHAR(50)  NOT NULL,
    payload         TEXT,
    response_status INT,
    response_body   TEXT,
    success         BOOLEAN      NOT NULL,
    error_message   TEXT,
    attempted_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_deliveries_webhook ON webhook_deliveries(webhook_id);
CREATE INDEX idx_webhook_deliveries_time    ON webhook_deliveries(attempted_at DESC);