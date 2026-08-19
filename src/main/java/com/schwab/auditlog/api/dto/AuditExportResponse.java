package com.schwab.auditlog.api.dto;

import java.time.Instant;
import java.util.List;

public record AuditExportResponse(
        String format,
        Instant exportedAt,
        AuditExportFilter filter,
        long chainLastSequence,
        String chainHeadHash,
        List<AuditEventResponse> events,
        List<ChainWitnessRecord> chainWitness) {
}
