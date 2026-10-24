CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(20)  NOT NULL CHECK (action IN ('CREATE','UPDATE','DELETE')),
    actor_email  VARCHAR(255),
    actor_role   VARCHAR(20),
    ip_address   VARCHAR(64),
    details      TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_actor  ON audit_logs(actor_email);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);