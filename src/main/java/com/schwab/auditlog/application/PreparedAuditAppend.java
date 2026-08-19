package com.schwab.auditlog.application;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record PreparedAuditAppend(UUID eventId, String eventType, String actorId, String resourceType, String resourceId,
                           Instant occurredAt, JsonNode payloadProjection, String payloadCommitment,
                           List<PreparedSensitivePayload> sensitivePayloads) {
}
