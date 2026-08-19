package com.schwab.auditlog.domain.redaction;

import com.fasterxml.jackson.databind.JsonNode;

public record SensitivePayloadValue(String jsonPointer, JsonNode value) {
}
