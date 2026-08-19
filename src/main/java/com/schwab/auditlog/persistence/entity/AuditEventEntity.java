package com.schwab.auditlog.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(name = "audit_event")
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_number", updatable = false)
    private Long sequenceNumber;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;
    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;
    @Column(name = "actor_id", nullable = false, updatable = false)
    private String actorId;
    @Column(name = "resource_type", nullable = false, updatable = false)
    private String resourceType;
    @Column(name = "resource_id", nullable = false, updatable = false)
    private String resourceId;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_projection", nullable = false, columnDefinition = "jsonb", updatable = false)
    private JsonNode payloadProjection;
    @Column(name = "payload_commitment", nullable = false, updatable = false)
    private String payloadCommitment;
    @Column(name = "previous_hash", nullable = false, updatable = false)
    private String previousHash;
    @Column(name = "event_hash", nullable = false, updatable = false)
    private String eventHash;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditEventEntity() {
    }

    public AuditEventEntity(UUID eventId, String eventType, String actorId, String resourceType,
                            String resourceId, Instant occurredAt, JsonNode payloadProjection,
                            String payloadCommitment, String previousHash, String eventHash, Instant createdAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.actorId = actorId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.occurredAt = occurredAt;
        this.payloadProjection = payloadProjection;
        this.payloadCommitment = payloadCommitment;
        this.previousHash = previousHash;
        this.eventHash = eventHash;
        this.createdAt = createdAt;
    }

    public Long getSequenceNumber() { return sequenceNumber; }
    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getActorId() { return actorId; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public Instant getOccurredAt() { return occurredAt; }
    public JsonNode getPayloadProjection() { return payloadProjection; }
    public String getPayloadCommitment() { return payloadCommitment; }
    public String getPreviousHash() { return previousHash; }
    public String getEventHash() { return eventHash; }
    public Instant getCreatedAt() { return createdAt; }
}
