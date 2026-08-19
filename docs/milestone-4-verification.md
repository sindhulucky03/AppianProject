# Milestone 4: chain verification

`GET /audit/verify` reads the chain state and every audit event in one PostgreSQL repeatable-read transaction. It streams events by insertion sequence, recalculates the hash of each immutable preimage, and compares every predecessor link. It returns immediately at the first inconsistency.

The final valid record is compared with `audit_chain_state`. This additional check detects deletion of the final record, which otherwise has no successor whose `previous_hash` could fail. The response distinguishes content tampering, predecessor tampering, and chain-head sequence/hash mismatches.

The integration test uses a disposable PostgreSQL Testcontainer. It first bypasses the append-only trigger deliberately, then changes an event directly in the database and asserts that the verification endpoint's service reports `CONTENT_HASH_MISMATCH`. In production, the application role must not have privileges to disable triggers; this test uses the container administrator solely to demonstrate tamper evidence.
