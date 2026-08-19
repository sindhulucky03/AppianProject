package com.schwab.auditlog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AuditEventPageResponse(
        List<AuditEventResponse> events,
        @Schema(description = "Opaque cursor for the next page; absent on the final page.") String nextCursor) {
}
