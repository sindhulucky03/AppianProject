package com.schwab.auditlog.application;

import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.api.dto.AuditExportResponse;
import com.schwab.auditlog.api.dto.ChainWitnessRecord;
import com.schwab.auditlog.api.dto.ExportVerificationResponse;
import com.schwab.auditlog.domain.hashing.AuditEventHashCalculator;
import com.schwab.auditlog.domain.hashing.AuditEventHashMaterial;
import com.schwab.auditlog.domain.hashing.HashConstants;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Verifies an exported bundle without access to the source service database. */
@Component
public class ExportBundleVerifier {

    private final AuditEventHashCalculator hashCalculator;

    public ExportBundleVerifier(AuditEventHashCalculator hashCalculator) {
        this.hashCalculator = hashCalculator;
    }

    public ExportVerificationResponse verify(AuditExportResponse bundle) {
        if (!"audit-log-export/v1".equals(bundle.format())) {
            return failed("Unsupported export format");
        }
        String expectedPreviousHash = HashConstants.GENESIS_HASH;
        long expectedSequence = 0;
        Map<Long, ChainWitnessRecord> witnesses = new HashMap<>();
        for (ChainWitnessRecord witness : bundle.chainWitness()) {
            if (witness.sequenceNumber() <= expectedSequence) {
                return failed("Witness sequence is not strictly increasing");
            }
            if (!expectedPreviousHash.equals(witness.previousHash())) {
                return failed("Witness predecessor hash mismatch at sequence " + witness.sequenceNumber());
            }
            witnesses.put(witness.sequenceNumber(), witness);
            expectedSequence = witness.sequenceNumber();
            expectedPreviousHash = witness.eventHash();
        }
        if (expectedSequence != bundle.chainLastSequence() || !expectedPreviousHash.equals(bundle.chainHeadHash())) {
            return failed("Witness does not match declared chain head");
        }
        for (AuditEventResponse event : bundle.events()) {
            ChainWitnessRecord witness = witnesses.get(event.sequenceNumber());
            if (witness == null || !witness.eventId().equals(event.eventId()) || !witness.eventHash().equals(event.eventHash())) {
                return failed("Selected event is absent or inconsistent with the witness at sequence " + event.sequenceNumber());
            }
            String calculatedHash = hashCalculator.eventHash(new AuditEventHashMaterial(
                    event.eventId(), event.eventType(), event.actorId(), event.resourceType(), event.resourceId(),
                    event.occurredAt(), event.payload(), event.payloadCommitment(), event.previousHash()));
            if (!calculatedHash.equals(event.eventHash())) {
                return failed("Selected event content hash mismatch at sequence " + event.sequenceNumber());
            }
        }
        return new ExportVerificationResponse(true, null);
    }

    private ExportVerificationResponse failed(String failure) {
        return new ExportVerificationResponse(false, failure);
    }
}
