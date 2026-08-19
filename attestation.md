# ATTESTATION

## Candidate Information

- **Full Name:** Meda Lakshmi Naga Sindhura
- **Email Address:** sindhu.lucky03@gmail.com
- **Assignment Title:** AI-Assisted Software Engineering System — Audit Log Service
- **Date Started:** 18-08-2026
- **Date Submitted:** 18-08-2026

## Submission Scope and Revision

- **Repository:** `AppianProject`
- **Branch:** `main`
- **Reviewed baseline commit:** `4bd039a6692e4d441df2819e7dec51c05644d664`
- **Delivery scope:** Audit Log Service source, Flyway migrations, tests, Docker/Compose configuration, CI workflow, and supporting documentation.

The final submission must be committed before archive creation. Replace the reviewed baseline commit above with the exact final submission commit if the working tree has changed after this attestation was prepared.

## Claim-to-Evidence Mapping

| Claim | Source and validation evidence |
| --- | --- |
| JWT scope enforcement and local-only development bypass | `security/ResourceServerSecurityConfiguration.java`, `security/DevelopmentSecurityConfiguration.java` |
| Immutable hash chain and tamper detection | `application/AuditAppendTransaction.java`, `application/ChainVerificationService.java`, `integration/ChainVerificationIntegrationTest.java` |
| Concurrent append ordering | `persistence/repository/AuditChainStateRepository.java`, `integration/ConcurrentAppendIntegrationTest.java` |
| Retention and crypto-erasure | `application/RetentionService.java`, `application/RedactionService.java`, `integration/RetentionAndRedactionIntegrationTest.java` |
| Export witness and synchronization | `application/AuditExportService.java`, `application/ExportBundleVerifier.java`, `integration/ExportAndSyncIntegrationTest.java` |
| Automated test and coverage evidence | `.github/workflows/ci.yml`, `pom.xml` (Surefire and JaCoCo artifacts) |

## Required Attestation

I, Meda Lakshmi Naga Sindhura, attest that this submission is my own individual work, completed on my own machine and accounts, and that it honestly reflects my development process and use of AI.

## Signature

Meda Lakshmi Naga Sindhura
