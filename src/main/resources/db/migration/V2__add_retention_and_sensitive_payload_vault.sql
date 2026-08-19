CREATE TABLE audit_event_lifecycle (
    event_id UUID PRIMARY KEY REFERENCES audit_event(event_id),
    archived_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(100) NOT NULL
);

CREATE TABLE audit_sensitive_payload (
    event_id UUID NOT NULL REFERENCES audit_event(event_id),
    json_pointer VARCHAR(1024) NOT NULL,
    ciphertext BYTEA NOT NULL,
    payload_iv BYTEA NOT NULL,
    encrypted_data_key BYTEA,
    key_iv BYTEA,
    redacted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (event_id, json_pointer),
    CONSTRAINT chk_sensitive_payload_key_state CHECK (
        (redacted_at IS NULL AND encrypted_data_key IS NOT NULL AND key_iv IS NOT NULL)
        OR (redacted_at IS NOT NULL AND encrypted_data_key IS NULL AND key_iv IS NULL)
    )
);

CREATE INDEX idx_audit_lifecycle_archived_at ON audit_event_lifecycle (archived_at);
