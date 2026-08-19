package com.schwab.auditlog.api.dto;

import java.util.UUID;

public record ChainViolationResponse(
        Long sequenceNumber,
        UUID eventId,
        ChainViolationType violation,
        String expected,
        String actual) {
}
