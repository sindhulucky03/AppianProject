-- Replace placeholders with representative values. Run only against a non-production copy.
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM audit_event
WHERE actor_id = 'advisor-123'
  AND occurred_at >= TIMESTAMPTZ '2026-01-01T00:00:00Z'
  AND (occurred_at, sequence_number) > (TIMESTAMPTZ '2026-02-01T00:00:00Z', 1000)
ORDER BY occurred_at, sequence_number
LIMIT 101;

EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM audit_event
WHERE resource_id = 'A-100'
ORDER BY sequence_number
LIMIT 10000;
