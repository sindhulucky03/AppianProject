# Milestone 5: retention and structured redaction

## Retention

Retention uses a configurable window (`AUDIT_RETENTION_WINDOW`, default 365 days) and a scheduled job. Expired records receive a row in `audit_event_lifecycle`; the immutable event is never changed or deleted. Normal event queries exclude archived records, while chain verification scans the full immutable table. This prevents legitimate archival from appearing as a broken chain.

## Structured redaction

At ingest, configured sensitive field names are replaced by `[REDACTED]` in the immutable `payload_projection`. Their original JSON values are individually encrypted with random AES-256-GCM data keys. Each data key is separately encrypted by the configured master key and stored with its JSON Pointer.

Redaction takes a list of JSON Pointers, removes the encrypted per-field data key and its IV, and records `redacted_at`. Ciphertext is retained but is no longer decryptable: this is crypto-erasure. Neither the event projection, its commitment to the original payload, nor its hash changes, so verification remains valid.

The configured local development master key is deliberately unsafe. A production deployment must source it from a KMS/HSM, tightly authorize the redaction endpoint, audit redaction requests, define backup/WAL expiry guarantees, and obtain legal/compliance approval for the retention period.
