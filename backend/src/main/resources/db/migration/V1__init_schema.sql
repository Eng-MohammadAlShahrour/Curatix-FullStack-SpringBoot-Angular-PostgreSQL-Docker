-- Curatix: initial schema
-- 4 tables: users, museums, artifacts, loan_requests

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','MUSEUM','REVIEWER')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE museums (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    country    VARCHAR(100) NOT NULL,
    city       VARCHAR(100),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE artifacts (
    id           BIGSERIAL PRIMARY KEY,
    museum_id    BIGINT       NOT NULL REFERENCES museums(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    era          VARCHAR(100),
    description  TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE'
                 CHECK (status IN ('AVAILABLE','ON_LOAN','RESERVED')),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_artifacts_museum ON artifacts(museum_id);

CREATE TABLE loan_requests (
    id              BIGSERIAL PRIMARY KEY,
    artifact_id     BIGINT       NOT NULL REFERENCES artifacts(id),
    from_museum_id  BIGINT       NOT NULL REFERENCES museums(id),
    to_museum_id    BIGINT       NOT NULL REFERENCES museums(id),
    requested_by    BIGINT       NOT NULL REFERENCES users(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','APPROVED','REJECTED','SHIPPED','RETURNED')),
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_loan_status ON loan_requests(status);
CREATE INDEX idx_loan_artifact ON loan_requests(artifact_id);