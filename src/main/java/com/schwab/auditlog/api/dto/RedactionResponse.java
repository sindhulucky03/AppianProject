package com.schwab.auditlog.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RedactionResponse(UUID eventId, List<String> redactedPaths, Instant redactedAt) {
}
