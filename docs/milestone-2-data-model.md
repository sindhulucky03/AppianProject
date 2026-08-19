# Milestone 2: immutable data model and hash contract

## Append concurrency

There is one global chain. A write transaction will lock the singleton `audit_chain_state` row with a pessimistic write lock, read its `last_hash`, insert one immutable `audit_event`, obtain its identity sequence, and advance the head before commit. This serializes hash-predecessor selection even when the API runs in multiple instances.

## Hash contract

`event_hash = SHA-256(UTF-8(canonical-json(preimage)))`. The preimage contains `eventId`, event/actor/resource fields, server-assigned `occurredAt`, `payloadProjection`, `payloadCommitment`, and `previousHash`. Object keys are recursively sorted, arrays retain order, and numeric values are normalized. All hashes are lowercase 64-character hexadecimal SHA-256 values. The first event references the 64-zero genesis hash.

`payloadCommitment` is SHA-256 over the canonical original payload. The future redaction design will store a safe projection in the immutable event and encrypted source data separately; deleting key material will not alter either hash input.

## Database protections

The database uses a trigger to reject normal event updates and deletes. This is defense in depth, not the only integrity guarantee: a privileged actor could bypass it. A verifier must therefore recompute every event hash, validate predecessor links, and compare the final sequence/hash with `audit_chain_state`. That detects removal of the final event as well as in-chain edits.

## Milestone 3 API contract

`POST /audit/events` accepts `eventType`, `actorId`, `resourceType`, `resourceId`, and an object `payload`. The service—not the caller—assigns the UTC `occurredAt` timestamp and generates an event UUID. `GET /audit/events` accepts any combination of `actorId`, `resourceType`, `resourceId`, `eventType`, `from`, and `to`. Sorting is fixed to `(occurredAt, sequenceNumber)` ascending. A URL-safe opaque cursor contains those two values, preventing skipped/duplicated rows for a fixed result set.
