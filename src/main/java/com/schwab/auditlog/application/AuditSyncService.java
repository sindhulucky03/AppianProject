package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.api.dto.AuditSyncResponse;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.schwab.auditlog.observability.AuditMetrics;

@Service
public class AuditSyncService {

    private final AuditEventRepository eventRepository;
    private final AuditEventMapper eventMapper;
    private final AuditMetrics metrics;

    public AuditSyncService(AuditEventRepository eventRepository, AuditEventMapper eventMapper, AuditMetrics metrics) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true)
    public AuditSyncResponse sync(long afterSequence, int limit) {
        long startedAt = System.nanoTime();
        List<AuditEventEntity> events = new ArrayList<>(eventRepository
                .findBySequenceNumberGreaterThanOrderBySequenceNumberAsc(afterSequence, PageRequest.of(0, limit + 1)));
        boolean hasMore = events.size() > limit;
        if (hasMore) {
            events.removeLast();
        }
        long nextAfterSequence = events.isEmpty() ? afterSequence : events.getLast().getSequenceNumber();
        List<AuditEventResponse> responses = events.stream().map(eventMapper::toResponse).toList();
        metrics.syncCompleted(System.nanoTime() - startedAt);
        return new AuditSyncResponse(responses, nextAfterSequence, hasMore);
    }
}
