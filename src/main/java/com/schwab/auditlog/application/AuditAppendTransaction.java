package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.domain.hashing.AuditEventHashCalculator;
import com.schwab.auditlog.domain.hashing.AuditEventHashMaterial;
import com.schwab.auditlog.persistence.entity.AuditChainStateEntity;
import com.schwab.auditlog.persistence.entity.AuditEventEntity;
import com.schwab.auditlog.persistence.entity.AuditSensitivePayloadEntity;
import com.schwab.auditlog.persistence.repository.AuditChainStateRepository;
import com.schwab.auditlog.persistence.repository.AuditEventRepository;
import com.schwab.auditlog.persistence.repository.AuditSensitivePayloadRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The shortest possible transaction that owns global-chain ordering. */
@Service
class AuditAppendTransaction {

    private final AuditChainStateRepository chainStateRepository;
    private final AuditEventRepository eventRepository;
    private final AuditSensitivePayloadRepository sensitivePayloadRepository;
    private final AuditEventHashCalculator hashCalculator;
    private final AuditEventMapper eventMapper;
    private final Clock clock;

    AuditAppendTransaction(AuditChainStateRepository chainStateRepository, AuditEventRepository eventRepository,
                           AuditSensitivePayloadRepository sensitivePayloadRepository,
                           AuditEventHashCalculator hashCalculator, AuditEventMapper eventMapper, Clock clock) {
        this.chainStateRepository = chainStateRepository;
        this.eventRepository = eventRepository;
        this.sensitivePayloadRepository = sensitivePayloadRepository;
        this.hashCalculator = hashCalculator;
        this.eventMapper = eventMapper;
        this.clock = clock;
    }

    @Transactional
    AuditEventResponse append(PreparedAuditAppend prepared) {
        AuditChainStateEntity chainState = chainStateRepository
                .findByChainIdForUpdate(AuditChainStateEntity.GLOBAL_CHAIN_ID)
                .orElseThrow(() -> new IllegalStateException("Global audit chain state is missing"));
        String previousHash = chainState.getLastHash();
        String eventHash = hashCalculator.eventHash(new AuditEventHashMaterial(prepared.eventId(), prepared.eventType(),
                prepared.actorId(), prepared.resourceType(), prepared.resourceId(), prepared.occurredAt(),
                prepared.payloadProjection(), prepared.payloadCommitment(), previousHash));
        AuditEventEntity saved = eventRepository.saveAndFlush(new AuditEventEntity(prepared.eventId(), prepared.eventType(),
                prepared.actorId(), prepared.resourceType(), prepared.resourceId(), prepared.occurredAt(),
                prepared.payloadProjection(), prepared.payloadCommitment(), previousHash, eventHash, prepared.occurredAt()));
        prepared.sensitivePayloads().forEach(payload -> sensitivePayloadRepository.save(
                new AuditSensitivePayloadEntity(prepared.eventId(), payload.jsonPointer(), payload.encryptedPayload(),
                        prepared.occurredAt())));
        chainState.advance(saved.getSequenceNumber(), eventHash, clock.instant());
        return eventMapper.toResponse(saved);
    }
}
