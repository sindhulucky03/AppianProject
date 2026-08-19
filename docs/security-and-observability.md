# Security and observability

## Security boundary

Security is fail-closed by default. Every profile except the explicitly selected `local` profile requires Spring Security's standard `spring.security.oauth2.resourceserver.jwt.issuer-uri`; without it, the application cannot start. The `local` profile is intended solely for developer machines and test execution, and is the only profile that permits unauthenticated access. In protected modes the API is stateless JWT resource-server protected:

| Operation | Required scope |
| --- | --- |
| Append event | `audit.write` |
| Query, verify, export, sync, OpenAPI | `audit.read` |
| Redact a field | `audit.admin` |
| Scrape Prometheus metrics | `audit.metrics` |

Only `/actuator/health/**` is anonymous. Production must also restrict Prometheus access at the network layer, use TLS/mTLS at the ingress, rotate KMS keys, grant the application database role no DDL/trigger privileges, and record the authenticated principal as an audit field in a future schema migration.

The current audit-event model has no tenant identifier or resource-owner claim, so scope authorization is the implemented boundary. Do not represent this as tenant isolation: adding that control requires an explicit tenant model, JWT claim contract, migration/backfill plan, and cross-tenant authorization tests.

## Safe telemetry

Every response receives an `X-Request-Id`; supplied IDs are accepted only if they match a bounded safe character set. The ID is placed in SLF4J MDC and the log pattern. Request/response bodies and sensitive payload values are never logged.

Actuator exposes health, metrics, and Prometheus metrics. Domain counters include appended events, archived events, crypto-erased payloads, and exports. Alerts should cover verification failures, retention job errors, error rate, database pool exhaustion, and elevated append latency.
