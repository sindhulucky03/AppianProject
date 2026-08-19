-- Matches keyset pagination ordered by occurred_at then sequence_number.
CREATE INDEX idx_audit_event_time_sequence ON audit_event (occurred_at, sequence_number);

-- Matches all-record exports, which are sorted by append sequence after filtering.
CREATE INDEX idx_audit_event_actor_sequence ON audit_event (actor_id, sequence_number);
CREATE INDEX idx_audit_event_resource_sequence ON audit_event (resource_id, sequence_number);
