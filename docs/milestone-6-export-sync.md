# Milestone 6: bulk export and incremental synchronization

## Bulk export

`GET /audit/export` accepts exactly one `actorId` or `resourceId` and returns every matching event, including archived records. The bundle contains each selected full event, plus a hash-only witness for the entire chain, genesis-linked through the declared chain head. A recipient can recompute selected content hashes, validate all witness links, and prove every selected record occupies the claimed position in that chain.

This is tamper-evident rather than independently anchored: a source that controls both the export and the database can construct a different self-consistent history. Production must sign periodic chain heads with a KMS key and anchor those signatures in an independent immutable system. The full witness also exposes event identifiers and chain length; a Merkle proof/checkpoint approach is a future privacy and size improvement.

## Incremental synchronization

`GET /audit/events/sync?afterSequence={watermark}` returns events with a higher append sequence and a `nextAfterSequence` watermark. Sequence numbers are monotonic and never reused; gaps from rolled-back PostgreSQL identity allocation are harmless. Consumers persist the returned watermark only after processing the entire response.
