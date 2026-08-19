package com.schwab.auditlog.api.dto;

import java.util.UUID;

/** Hash-only chain metadata included to verify selected records in an export. */
public record ChainWitnessRecord(long sequenceNumber, UUID eventId, String previousHash, String eventHash) {
}
