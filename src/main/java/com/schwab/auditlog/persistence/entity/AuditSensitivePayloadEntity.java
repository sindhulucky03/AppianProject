package com.schwab.auditlog.persistence.entity;

import com.schwab.auditlog.domain.redaction.EncryptedPayload;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_sensitive_payload")
public class AuditSensitivePayloadEntity {

    @EmbeddedId
    private AuditSensitivePayloadId id;
    @Column(nullable = false)
    private byte[] ciphertext;
    @Column(name = "payload_iv", nullable = false)
    private byte[] payloadIv;
    @Column(name = "encrypted_data_key")
    private byte[] encryptedDataKey;
    @Column(name = "key_iv")
    private byte[] keyIv;
    @Column(name = "redacted_at")
    private Instant redactedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditSensitivePayloadEntity() {
    }

    public AuditSensitivePayloadEntity(UUID eventId, String jsonPointer, EncryptedPayload encryptedPayload, Instant createdAt) {
        this.id = new AuditSensitivePayloadId(eventId, jsonPointer);
        this.ciphertext = encryptedPayload.ciphertext();
        this.payloadIv = encryptedPayload.payloadIv();
        this.encryptedDataKey = encryptedPayload.encryptedDataKey();
        this.keyIv = encryptedPayload.keyIv();
        this.createdAt = createdAt;
    }

    public String getJsonPointer() { return id.getJsonPointer(); }
    public boolean isRedacted() { return redactedAt != null; }

    public void cryptographicallyErase(Instant timestamp) {
        this.encryptedDataKey = null;
        this.keyIv = null;
        this.redactedAt = timestamp;
    }
}
