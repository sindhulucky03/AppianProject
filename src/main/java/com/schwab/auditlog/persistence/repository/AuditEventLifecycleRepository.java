package com.schwab.auditlog.persistence.repository;

import com.schwab.auditlog.persistence.entity.AuditEventLifecycleEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventLifecycleRepository extends JpaRepository<AuditEventLifecycleEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO audit_event_lifecycle (event_id, archived_at, reason)
            SELECT event_id, :archivedAt, 'RETENTION_POLICY'
            FROM audit_event event
            WHERE event.occurred_at < :cutoff
              AND NOT EXISTS (SELECT 1 FROM audit_event_lifecycle lifecycle WHERE lifecycle.event_id = event.event_id)
            """, nativeQuery = true)
    int archiveBefore(@Param("cutoff") Instant cutoff, @Param("archivedAt") Instant archivedAt);
}
