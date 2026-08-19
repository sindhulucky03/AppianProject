# Performance design and validation

## Implemented optimizations

| Area | Decision | Why |
| --- | --- | --- |
| Append path | Canonicalization, payload commitment, sensitive-field projection, and AES encryption happen before the chain transaction | Reduces the time the global chain-head lock is held |
| Ordering | A minimal transaction locks only the chain head, writes the event/vault records, then advances the head | Preserves strict global ordering and transaction atomicity |
| Query paging | Keyset cursor uses `(occurredAt, sequenceNumber)` | Avoids large-offset scans and duplicate/skip behavior during writes |
| Query indexes | Time/sequence, actor/sequence, and resource/sequence indexes complement existing filter indexes | Matches pagination, export, and sync access paths |
| Verification | JPA streams records instead of loading the full chain | Keeps JVM memory bounded for full-chain scans |
| Telemetry | Micrometer timers cover append, query, verify, export, and sync | Enables evidence-based tuning rather than speculative changes |
| Pooling | Hikari has configurable minimum idle, maximum pool, and bounded connection waits | Avoids unbounded database contention |

## Constraints and scale path

A single global hash chain necessarily serializes final append ordering. This is correct for the assignment and ideal for moderate throughput. At higher throughput, partition chains by tenant/account and publish KMS-signed global checkpoints. That trades a single total order for per-partition order plus independently verifiable roots.

Exports currently materialize their full witness because the API returns one JSON document. For very large exports, provide an asynchronous NDJSON/ZIP export backed by object storage, signed checkpoints, and Merkle inclusion proofs. Do not silently page a “bulk export all” endpoint, because that weakens its completeness semantics.

## Benchmark procedure

1. Run PostgreSQL with production-like CPU, disk, and connection limits.
2. Use a load generator to issue realistic append/query mixes with representative payload sizes.
3. Capture p50/p95/p99 append and query timers from `/actuator/prometheus` and PostgreSQL lock/wait statistics.
4. Inspect the documented `EXPLAIN (ANALYZE, BUFFERS)` plans before adding or removing indexes.
5. Load-test verification/export separately; they intentionally scan chain history.

Initial service-level targets to validate with stakeholders: p95 append under 100 ms at expected load, p95 filtered query under 200 ms for a 100-row page, and no sustained Hikari connection-pool wait. These are targets, not measured claims.
