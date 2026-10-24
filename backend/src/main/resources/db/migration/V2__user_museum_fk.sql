ALTER TABLE users
    ADD COLUMN museum_id BIGINT REFERENCES museums(id) ON DELETE SET NULL;

CREATE INDEX idx_users_museum ON users(museum_id);