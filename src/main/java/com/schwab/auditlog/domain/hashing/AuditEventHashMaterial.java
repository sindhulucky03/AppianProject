package com.schwab.auditlog.domain.hashing;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/** Immutable fields included in an audit event's hash preimage. */
public record AuditEventHashMaterial(
        UUID eventId,
        String eventType,
        String actorId,
        String resourceType,
        String resourceId,
        Instant occurredAt,
        JsonNode payloadProjection,
        String payloadCommitment,
        String previousHash) {
}
