package com.schwab.auditlog.domain.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record PayloadProjection(JsonNode safePayload, List<SensitivePayloadValue> sensitiveValues) {
}
