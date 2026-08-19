package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.api.dto.AuditExportFilter;
import com.schwab.auditlog.api.dto.AuditExportResponse;
import com.schwab.auditlog.api.dto.ChainWitnessRecord;
import com.schwab.auditlog.persistence.entity.AuditChainStateEntity;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.repository.AuditChainStateRepository;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;
import com.schwab.auditlog.observability.AuditMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditExportService {

    private final AuditEventRepository eventRepository;
    private final AuditChainStateRepository chainStateRepository;
    private final AuditEventMapper eventMapper;
    private final Clock clock;
    private final AuditMetrics metrics;

    public AuditExportService(AuditEventRepository eventRepository, AuditChainStateRepository chainStateRepository,
                              AuditEventMapper eventMapper, Clock clock, AuditMetrics metrics) {
        this.eventRepository = eventRepository;
        this.chainStateRepository = chainStateRepository;
        this.eventMapper = eventMapper;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AuditExportResponse export(AuditExportFilter filter) {
        long startedAt = System.nanoTime();
        boolean hasActor = filter.actorId() != null && !filter.actorId().isBlank();
        boolean hasResource = filter.resourceId() != null && !filter.resourceId().isBlank();
        if (hasActor == hasResource) {
            throw new IllegalArgumentException("Specify exactly one of actorId or resourceId");
        }
        AuditChainStateEntity chainState = chainStateRepository.findById(AuditChainStateEntity.GLOBAL_CHAIN_ID)
                .orElseThrow(() -> new IllegalStateException("Global audit chain state is missing"));
        List<AuditEventEntity> selected = hasActor
                ? eventRepository.findByActorIdOrderBySequenceNumberAsc(filter.actorId())
                : eventRepository.findByResourceIdOrderBySequenceNumberAsc(filter.resourceId());
        List<ChainWitnessRecord> witness;
        try (Stream<AuditEventEntity> allEvents = eventRepository.streamAllBySequenceNumberAsc()) {
            witness = allEvents.map(event -> new ChainWitnessRecord(event.getSequenceNumber(), event.getEventId(),
                    event.getPreviousHash(), event.getEventHash())).toList();
        }
        List<AuditEventResponse> events = selected.stream().map(eventMapper::toResponse).toList();
        metrics.exportCreated();
        metrics.exportCompleted(System.nanoTime() - startedAt);
        return new AuditExportResponse("audit-log-export/v1", clock.instant(), filter, chainState.getLastSequence(),
                chainState.getLastHash(), events, witness);
    }
}
