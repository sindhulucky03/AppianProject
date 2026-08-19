package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.RedactPayloadRequest;
import com.schwab.auditlog.api.dto.RedactionResponse;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import com.schwab.auditlog.persistence.repository.AuditSensitivePayloadRepository;
import com.schwab.auditlog.observability.AuditMetrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedactionService {

    private final AuditEventRepository eventRepository;
    private final AuditSensitivePayloadRepository sensitivePayloadRepository;
    private final Clock clock;
    private final AuditMetrics metrics;

    public RedactionService(AuditEventRepository eventRepository, AuditSensitivePayloadRepository sensitivePayloadRepository,
                            Clock clock, AuditMetrics metrics) {
        this.eventRepository = eventRepository;
        this.sensitivePayloadRepository = sensitivePayloadRepository;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Transactional
    public RedactionResponse redact(UUID eventId, RedactPayloadRequest request) {
        if (!eventRepository.existsByEventId(eventId)) {
            throw new IllegalArgumentException("Audit event does not exist");
        }
        var values = sensitivePayloadRepository.findByIdEventIdAndIdJsonPointerIn(eventId, request.jsonPointers());
        if (values.size() != request.jsonPointers().stream().distinct().count()) {
            throw new IllegalArgumentException("One or more requested paths are not redactable sensitive fields");
        }
        Instant redactedAt = clock.instant();
        values.stream().filter(value -> !value.isRedacted()).forEach(value -> value.cryptographicallyErase(redactedAt));
        metrics.payloadsRedacted(values.size());
        return new RedactionResponse(eventId, values.stream().map(value -> value.getJsonPointer()).toList(), redactedAt);
    }
}
