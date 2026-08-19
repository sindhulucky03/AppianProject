package com.schwab.auditlog.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "A request to append an immutable audit event. The server assigns occurredAt.")
public record CreateAuditEventRequest(
        @NotBlank @Size(max = 100) @Schema(example = "ACCOUNT_VIEWED") String eventType,
        @NotBlank @Size(max = 255) @Schema(example = "advisor-123") String actorId,
        @NotBlank @Size(max = 100) @Schema(example = "account") String resourceType,
        @NotBlank @Size(max = 255) @Schema(example = "A-100") String resourceId,
        @NotNull @Schema(example = "{\"reason\":\"annual review\"}") JsonNode payload) {
}
