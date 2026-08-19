# AI usage traceability

AI assistance was used for initial project structure, code drafts, test-case ideas, dependency compatibility research, and documentation drafts. The engineer selected the architecture, reviewed and refined all code, chose the append lock and redaction design, and owns final correctness.

| Activity | AI contribution | Engineer review |
| --- | --- | --- |
| Architecture | Proposed modular Spring design and milestone decomposition | Selected PostgreSQL, one global chain, server timestamp, and scope boundaries |
| Hash chain | Drafted canonicalization and verification structure | Defined preimage, chain-head comparison, and tamper test |
| Privacy | Drafted safe projection and crypto-erasure mechanics | Selected per-field data keys and documented KMS limitations |
| Quality | Drafted test and CI outlines | Kept Testcontainers, reviewed failure modes, and documented unexecuted local state |

No customer data, credentials, or proprietary code was supplied to AI. Security-sensitive changes require engineer, security, and compliance review before production deployment.
