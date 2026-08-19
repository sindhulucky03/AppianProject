package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.ChainVerificationResponse;
import com.schwab.auditlog.api.dto.ChainViolationResponse;
import com.schwab.auditlog.api.dto.ChainViolationType;
import com.schwab.auditlog.domain.hashing.AuditEventHashCalculator;
import com.schwab.auditlog.domain.hashing.AuditEventHashMaterial;
import com.schwab.auditlog.domain.hashing.HashConstants;
import com.schwab.auditlog.persistence.entity.AuditChainStateEntity;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.repository.AuditChainStateRepository;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import java.time.Clock;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.schwab.auditlog.observability.AuditMetrics;

@Service
public class ChainVerificationService {

    private final AuditChainStateRepository chainStateRepository;
    private final AuditEventRepository eventRepository;
    private final AuditEventHashCalculator hashCalculator;
    private final Clock clock;
    private final AuditMetrics metrics;

    public ChainVerificationService(AuditChainStateRepository chainStateRepository, AuditEventRepository eventRepository,
                                    AuditEventHashCalculator hashCalculator, Clock clock, AuditMetrics metrics) {
        this.chainStateRepository = chainStateRepository;
        this.eventRepository = eventRepository;
        this.hashCalculator = hashCalculator;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ChainVerificationResponse verify() {
        long startedAt = System.nanoTime();
        try {
            return verifyChain();
        } finally {
            metrics.verificationCompleted(System.nanoTime() - startedAt);
        }
    }

    private ChainVerificationResponse verifyChain() {
        AuditChainStateEntity chainState = chainStateRepository.findById(AuditChainStateEntity.GLOBAL_CHAIN_ID)
                .orElseThrow(() -> new IllegalStateException("Global audit chain state is missing"));
        String expectedPreviousHash = HashConstants.GENESIS_HASH;
        long recordCount = 0;
        long finalSequence = 0;

        try (Stream<AuditEventEntity> events = eventRepository.streamAllBySequenceNumberAsc()) {
            var iterator = events.iterator();
            while (iterator.hasNext()) {
                AuditEventEntity event = iterator.next();
                recordCount++;
                finalSequence = event.getSequenceNumber();
                if (!expectedPreviousHash.equals(event.getPreviousHash())) {
                    return failed(recordCount, expectedPreviousHash, event.getPreviousHash(), event,
                            ChainViolationType.PREVIOUS_HASH_MISMATCH);
                }
                String calculatedHash = hashCalculator.eventHash(new AuditEventHashMaterial(
                        event.getEventId(), event.getEventType(), event.getActorId(), event.getResourceType(),
                        event.getResourceId(), event.getOccurredAt(), event.getPayloadProjection(),
                        event.getPayloadCommitment(), event.getPreviousHash()));
                if (!calculatedHash.equals(event.getEventHash())) {
                    return failed(recordCount, calculatedHash, event.getEventHash(), event,
                            ChainViolationType.CONTENT_HASH_MISMATCH);
                }
                expectedPreviousHash = event.getEventHash();
            }
        }

        if (chainState.getLastSequence() != finalSequence) {
            return new ChainVerificationResponse(false, recordCount, expectedPreviousHash, clock.instant(),
                    new ChainViolationResponse(null, null, ChainViolationType.CHAIN_HEAD_SEQUENCE_MISMATCH,
                            String.valueOf(finalSequence), String.valueOf(chainState.getLastSequence())));
        }
        if (!chainState.getLastHash().equals(expectedPreviousHash)) {
            return new ChainVerificationResponse(false, recordCount, expectedPreviousHash, clock.instant(),
                    new ChainViolationResponse(null, null, ChainViolationType.CHAIN_HEAD_HASH_MISMATCH,
                            expectedPreviousHash, chainState.getLastHash()));
        }
        return new ChainVerificationResponse(true, recordCount, expectedPreviousHash, clock.instant(), null);
    }

    private ChainVerificationResponse failed(long recordCount, String expected, String actual, AuditEventEntity event,
                                             ChainViolationType type) {
        return new ChainVerificationResponse(false, recordCount, event.getEventHash(), clock.instant(),
                new ChainViolationResponse(event.getSequenceNumber(), event.getEventId(), type, expected, actual));
    }
}
