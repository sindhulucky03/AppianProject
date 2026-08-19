package com.schwab.auditlog.domain.redaction;

public record EncryptedPayload(byte[] ciphertext, byte[] payloadIv, byte[] encryptedDataKey, byte[] keyIv) {
}
