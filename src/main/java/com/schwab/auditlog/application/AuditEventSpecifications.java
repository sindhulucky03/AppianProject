package com.schwab.auditlog.application;

import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.entity.AuditEventLifecycleEntity;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

final class AuditEventSpecifications {

    private AuditEventSpecifications() {
    }

    static Specification<AuditEventEntity> matching(AuditEventQuery query, AuditEventCursor cursor) {
        return has("actorId", query.actorId())
                .and(notArchived())
                .and(has("resourceType", query.resourceType()))
                .and(has("resourceId", query.resourceId()))
                .and(has("eventType", query.eventType()))
                .and(from(query.from()))
                .and(to(query.to()))
                .and(after(cursor));
    }

    private static Specification<AuditEventEntity> notArchived() {
        return (root, query, builder) -> {
            Subquery<UUID> archivedEvent = query.subquery(UUID.class);
            Root<AuditEventLifecycleEntity> lifecycle = archivedEvent.from(AuditEventLifecycleEntity.class);
            archivedEvent.select(lifecycle.get("eventId"))
                    .where(builder.equal(lifecycle.get("eventId"), root.get("eventId")));
            return builder.not(builder.exists(archivedEvent));
        };
    }

    private static Specification<AuditEventEntity> has(String property, String value) {
        return (root, query, builder) -> value == null ? builder.conjunction() : builder.equal(root.get(property), value);
    }

    private static Specification<AuditEventEntity> from(Instant from) {
        return (root, query, builder) -> from == null ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.<Instant>get("occurredAt"), from);
    }

    private static Specification<AuditEventEntity> to(Instant to) {
        return (root, query, builder) -> to == null ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.<Instant>get("occurredAt"), to);
    }

    private static Specification<AuditEventEntity> after(AuditEventCursor cursor) {
        return (root, query, builder) -> {
            if (cursor == null) {
                return builder.conjunction();
            }
            return builder.or(
                    builder.greaterThan(root.<Instant>get("occurredAt"), cursor.occurredAt()),
                    builder.and(builder.equal(root.get("occurredAt"), cursor.occurredAt()),
                            builder.greaterThan(root.<Long>get("sequenceNumber"), cursor.sequenceNumber())));
        };
    }
}
