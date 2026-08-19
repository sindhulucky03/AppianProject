package com.schwab.auditlog.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "An immutable, hash-chained audit event.")
public record AuditEventResponse(
        long sequenceNumber,
        UUID eventId,
        String eventType,
        String actorId,
        String resourceType,
        String resourceId,
        Instant occurredAt,
        JsonNode payload,
        String payloadCommitment,
        String previousHash,
        String eventHash) {
}
