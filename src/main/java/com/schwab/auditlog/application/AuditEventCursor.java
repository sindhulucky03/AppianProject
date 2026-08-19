package com.schwab.auditlog.application;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/** Opaque, URL-safe continuation token for the fixed audit-event sort order. */
public record AuditEventCursor(Instant occurredAt, long sequenceNumber) {

    public String encode() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((occurredAt + "|" + sequenceNumber).getBytes(StandardCharsets.UTF_8));
    }

    public static AuditEventCursor decode(String encoded) {
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split("\\|", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("cursor must contain timestamp and sequence");
            }
            return new AuditEventCursor(Instant.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid cursor", exception);
        }
    }
}
