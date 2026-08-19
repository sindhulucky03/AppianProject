package com.schwab.auditlog.api.dto;

import java.time.Instant;

public record ChainVerificationResponse(
        boolean intact,
        long recordCount,
        String headHash,
        Instant verifiedAt,
        ChainViolationResponse firstInconsistency) {
}
