package com.schwab.auditlog.application;

import java.time.Instant;

public record AuditEventQuery(
        String actorId,
        String resourceType,
        String resourceId,
        String eventType,
        Instant from,
        Instant to,
        int limit,
        String cursor) {
}
