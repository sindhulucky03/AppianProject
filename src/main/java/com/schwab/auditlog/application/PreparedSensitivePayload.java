package com.schwab.auditlog.application;

import com.schwab.auditlog.domain.redaction.EncryptedPayload;

record PreparedSensitivePayload(String jsonPointer, EncryptedPayload encryptedPayload) {
}
