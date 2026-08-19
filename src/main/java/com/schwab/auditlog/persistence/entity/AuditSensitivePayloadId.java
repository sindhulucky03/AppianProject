package com.schwab.auditlog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AuditSensitivePayloadId implements Serializable {

    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "json_pointer")
    private String jsonPointer;

    protected AuditSensitivePayloadId() {
    }

    public AuditSensitivePayloadId(UUID eventId, String jsonPointer) {
        this.eventId = eventId;
        this.jsonPointer = jsonPointer;
    }

    public String getJsonPointer() { return jsonPointer; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof AuditSensitivePayloadId id
                && Objects.equals(eventId, id.eventId) && Objects.equals(jsonPointer, id.jsonPointer));
    }
    @Override public int hashCode() { return Objects.hash(eventId, jsonPointer); }
}
