package com.schwab.auditlog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event_lifecycle")
public class AuditEventLifecycleEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "archived_at", nullable = false)
    private Instant archivedAt;
    @Column(nullable = false)
    private String reason;

    protected AuditEventLifecycleEntity() {
    }
}
