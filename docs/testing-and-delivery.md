# Testing and delivery

JUnit 5 unit tests validate canonical JSON/hash determinism and cursor behavior. PostgreSQL Testcontainers integration tests validate the actual Flyway schema and JPA mappings, direct datastore tamper detection, archival without false chain failure, crypto-erasure without chain failure, export verification, and incremental synchronization.

Run locally with Docker available:

```powershell
docker compose up -d postgres
mvn verify
mvn spring-boot:run
```

For an application container use `docker compose up --build`. CI runs the full Maven verification suite on Java 21 and builds the Docker image. Production deployment should add image vulnerability scanning, dependency/SBOM scanning, signed images, environment-specific secret injection, and a migration rollback/runbook review.
