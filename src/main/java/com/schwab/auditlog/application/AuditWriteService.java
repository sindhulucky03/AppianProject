package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.domain.hashing.AuditEventHashCalculator;
import com.schwab.auditlog.domain.redaction.PayloadCryptoService;
import com.schwab.auditlog.domain.redaction.PayloadRedactionPolicy;
import com.schwab.auditlog.observability.AuditMetrics;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditWriteService {

    private final PayloadRedactionPolicy redactionPolicy;
    private final PayloadCryptoService payloadCryptoService;
    private final AuditEventHashCalculator hashCalculator;
    private final AuditAppendTransaction appendTransaction;
    private final Clock clock;
    private final AuditMetrics metrics;

    public AuditWriteService(PayloadRedactionPolicy redactionPolicy, PayloadCryptoService payloadCryptoService,
                             AuditEventHashCalculator hashCalculator, AuditAppendTransaction appendTransaction,
                             Clock clock, AuditMetrics metrics) {
        this.redactionPolicy = redactionPolicy;
        this.payloadCryptoService = payloadCryptoService;
        this.hashCalculator = hashCalculator;
        this.appendTransaction = appendTransaction;
        this.clock = clock;
        this.metrics = metrics;
    }

    public AuditEventResponse append(CreateAuditEventRequest request) {
        long startedAt = System.nanoTime();
        if (!request.payload().isObject()) {
            throw new IllegalArgumentException("payload must be a JSON object");
        }
        Instant now = clock.instant();
        UUID eventId = UUID.randomUUID();
        var projection = redactionPolicy.project(request.payload());
        var payloadProjection = projection.safePayload();
        String payloadCommitment = hashCalculator.payloadCommitment(request.payload());
        var sensitivePayloads = projection.sensitiveValues().stream()
                .map(value -> new PreparedSensitivePayload(value.jsonPointer(), payloadCryptoService.encrypt(value.value())))
                .toList();
        AuditEventResponse response = appendTransaction.append(new PreparedAuditAppend(eventId, request.eventType(),
                request.actorId(), request.resourceType(), request.resourceId(), now, payloadProjection,
                payloadCommitment, sensitivePayloads));
        metrics.eventAppended();
        metrics.appendCompleted(System.nanoTime() - startedAt);
        return response;
    }
}
