package com.schwab.auditlog.api.dto;

import java.util.List;

public record AuditSyncResponse(List<AuditEventResponse> events, long nextAfterSequence, boolean hasMore) {
}
