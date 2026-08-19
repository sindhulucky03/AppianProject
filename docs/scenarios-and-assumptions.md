# Assignment scenarios and assumptions

## Scenario A — core audit logging

Implemented: append-only event writes, all required event fields, server-assigned UTC timestamp, combined filters, cursor pagination, canonical SHA-256 predecessor chain, and full-chain verification. Direct privileged data-store edits are detected by integration tests.

## Scenario B — retention, redaction, and export

Implemented: soft archival under a configurable scheduler, per-field crypto-erasure with immutable safe projection, and complete actor/resource export bundles with independent verification logic. Production scope still requires KMS/HSM, external signed chain-head anchors, and compliance-approved retention/legal-hold processes.

## Scenario C — compliance reporting

Clarified requirement: *Authorized compliance analysts must retrieve a complete, tamper-evident account-access history for a client account over a time range without exposure of sensitive payload values by default.*

Assumptions: `resourceType=account` and `resourceId` identify the account; upstream identity supplies OAuth2 JWTs; server receipt time is authoritative. Implemented scope is filtering, verification, export, redaction, retention, and scope-based authorization. Out of scope: regulator-specific report formats, external key management, multi-region durability, legal holds, and independent anchoring.
