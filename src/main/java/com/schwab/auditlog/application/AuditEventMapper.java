package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {

    public AuditEventResponse toResponse(AuditEventEntity event) {
        return new AuditEventResponse(
                event.getSequenceNumber(), event.getEventId(), event.getEventType(), event.getActorId(),
                event.getResourceType(), event.getResourceId(), event.getOccurredAt(), event.getPayloadProjection(),
                event.getPayloadCommitment(), event.getPreviousHash(), event.getEventHash());
    }
}
