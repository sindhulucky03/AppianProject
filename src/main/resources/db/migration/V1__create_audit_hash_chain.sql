CREATE TABLE audit_chain_state (
    chain_id SMALLINT PRIMARY KEY,
    last_sequence BIGINT NOT NULL,
    last_hash CHAR(64) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_audit_chain_state_singleton CHECK (chain_id = 1),
    CONSTRAINT chk_audit_chain_state_sequence CHECK (last_sequence >= 0),
    CONSTRAINT chk_audit_chain_state_hash CHECK (last_hash ~ '^[0-9a-f]{64}$')
);

CREATE TABLE audit_event (
    sequence_number BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    actor_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    payload_projection JSONB NOT NULL,
    payload_commitment CHAR(64) NOT NULL,
    previous_hash CHAR(64) NOT NULL,
    event_hash CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_audit_event_hashes CHECK (
        payload_commitment ~ '^[0-9a-f]{64}$'
        AND previous_hash ~ '^[0-9a-f]{64}$'
        AND event_hash ~ '^[0-9a-f]{64}$'
    )
);

CREATE INDEX idx_audit_event_actor_time ON audit_event (actor_id, occurred_at, sequence_number);
CREATE INDEX idx_audit_event_resource_time ON audit_event (resource_type, resource_id, occurred_at, sequence_number);
CREATE INDEX idx_audit_event_type_time ON audit_event (event_type, occurred_at, sequence_number);

INSERT INTO audit_chain_state (chain_id, last_sequence, last_hash, updated_at)
VALUES (1, 0, repeat('0', 64), CURRENT_TIMESTAMP);

CREATE FUNCTION prevent_audit_event_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_event is append-only; updates and deletes are prohibited';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_audit_event_mutation
BEFORE UPDATE OR DELETE ON audit_event
FOR EACH ROW EXECUTE FUNCTION prevent_audit_event_mutation();
