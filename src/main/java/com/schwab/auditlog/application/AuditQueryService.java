package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventPageResponse;
import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.schwab.auditlog.observability.AuditMetrics;

@Service
public class AuditQueryService {

    private static final Sort EVENT_ORDER = Sort.by("occurredAt").ascending().and(Sort.by("sequenceNumber").ascending());
    private final AuditEventRepository eventRepository;
    private final AuditEventMapper eventMapper;
    private final AuditMetrics metrics;

    public AuditQueryService(AuditEventRepository eventRepository, AuditEventMapper eventMapper, AuditMetrics metrics) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true)
    public AuditEventPageResponse query(AuditEventQuery query) {
        long startedAt = System.nanoTime();
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        AuditEventCursor cursor = query.cursor() == null ? null : AuditEventCursor.decode(query.cursor());
        List<AuditEventEntity> results = new ArrayList<>(eventRepository.findAll(
                AuditEventSpecifications.matching(query, cursor), PageRequest.of(0, query.limit() + 1, EVENT_ORDER)).getContent());
        boolean hasNext = results.size() > query.limit();
        if (hasNext) {
            results.removeLast();
        }
        List<AuditEventResponse> events = results.stream().map(eventMapper::toResponse).toList();
        String nextCursor = hasNext ? toCursor(results.getLast()) : null;
        metrics.queryCompleted(System.nanoTime() - startedAt);
        return new AuditEventPageResponse(events, nextCursor);
    }

    private String toCursor(AuditEventEntity event) {
        return new AuditEventCursor(event.getOccurredAt(), event.getSequenceNumber()).encode();
    }
}
