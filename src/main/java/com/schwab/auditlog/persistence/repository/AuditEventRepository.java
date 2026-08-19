package com.schwab.auditlog.persistence.repository;

import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import java.util.UUID;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long>, JpaSpecificationExecutor<AuditEventEntity> {

    boolean existsByEventId(UUID eventId);

    @Query("select event from AuditEventEntity event order by event.sequenceNumber asc")
    Stream<AuditEventEntity> streamAllBySequenceNumberAsc();

    List<AuditEventEntity> findByActorIdOrderBySequenceNumberAsc(String actorId);

    List<AuditEventEntity> findByResourceIdOrderBySequenceNumberAsc(String resourceId);

    List<AuditEventEntity> findBySequenceNumberGreaterThanOrderBySequenceNumberAsc(long sequenceNumber,
                                                                                     org.springframework.data.domain.Pageable pageable);
}
