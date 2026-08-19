package com.schwab.auditlog.persistence.repository;

import com.schwab.auditlog.persistence.entity.AuditSensitivePayloadEntity;
import com.schwab.auditlog.persistence.entity.AuditSensitivePayloadId;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditSensitivePayloadRepository extends JpaRepository<AuditSensitivePayloadEntity, AuditSensitivePayloadId> {

    List<AuditSensitivePayloadEntity> findByIdEventIdAndIdJsonPointerIn(UUID eventId, Collection<String> jsonPointers);
}
