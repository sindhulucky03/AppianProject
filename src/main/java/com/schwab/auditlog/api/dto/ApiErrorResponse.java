package com.schwab.auditlog.api.dto;

import java.time.Instant;

public record ApiErrorResponse(Instant timestamp, int status, String error, String detail) {
}
