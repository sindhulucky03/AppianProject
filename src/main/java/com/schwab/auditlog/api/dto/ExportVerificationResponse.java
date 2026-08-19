package com.schwab.auditlog.api.dto;

public record ExportVerificationResponse(boolean intact, String failure) {
}
