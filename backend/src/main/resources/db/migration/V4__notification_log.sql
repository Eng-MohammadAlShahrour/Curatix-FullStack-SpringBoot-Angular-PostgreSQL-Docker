CREATE TABLE notification_logs (
    id             BIGSERIAL PRIMARY KEY,
    recipient      VARCHAR(255) NOT NULL,
    subject        VARCHAR(500) NOT NULL,
    body           TEXT,
    event_type     VARCHAR(50)  NOT NULL,
    status         VARCHAR(20)  NOT NULL CHECK (status IN ('SENT','FAILED','SKIPPED')),
    error_message  TEXT,
    sent_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_recipient ON notification_logs(recipient);
CREATE INDEX idx_notif_event     ON notification_logs(event_type);
CREATE INDEX idx_notif_sent_at   ON notification_logs(sent_at DESC);