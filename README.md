# Audit Log Service

Java 21 / Spring Boot 4.1.0 implementation of the Audit Log Service assignment.

## Milestone 1 — bootstrap

Prerequisites: Java 21, Maven 3.6.3+, and Docker Desktop. Spring Boot 4.1.0 supports Java 21 and Maven 3.6.3 or later. [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)

```powershell
docker compose up -d postgres
mvn clean verify
mvn -Dspring-boot.run.profiles=local spring-boot:run
```

Health is available at `http://localhost:8080/actuator/health`; Swagger UI will be at `http://localhost:8080/swagger-ui/index.html` once API endpoints are added.

Copy `.env.example` to `.env` only if you need to override Docker defaults. Never commit real credentials.

Milestone 2 defines the [immutable event schema and hash contract](docs/milestone-2-data-model.md).

Milestone 4 implements [full-chain verification](docs/milestone-4-verification.md) at `GET /audit/verify`.

Milestone 5 adds [retention and structured redaction](docs/milestone-5-retention-redaction.md). Sensitive fields configured in `audit.redaction.sensitive-field-names` are returned as `[REDACTED]`; request crypto-erasure with `POST /audit/events/{eventId}/redactions`.

Milestone 6 provides [verifiable export bundles and incremental synchronization](docs/milestone-6-export-sync.md): `GET /audit/export` and `GET /audit/events/sync`.

## Production controls and assignment evidence

- [Security and observability](docs/security-and-observability.md)
- [Testing and delivery](docs/testing-and-delivery.md)
- [Scenario clarification and scope](docs/scenarios-and-assumptions.md)
- [AI usage traceability](docs/ai-usage-log.md)
- [Final engineering summary](docs/final-engineering-summary.md)
- [Performance design and benchmark guidance](docs/performance.md)

Security is fail-closed by default: outside the explicit `local` profile, set `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` and supply JWTs with `audit.read`, `audit.write`, `audit.admin`, or `audit.metrics` scopes. The local profile is the only mode that permits unauthenticated requests; never deploy it or the development redaction key.

`mvn verify` produces JaCoCo coverage at `target/site/jacoco/index.html` and Surefire results at `target/surefire-reports`. CI uploads both as build artifacts.
