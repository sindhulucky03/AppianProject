package com.schwab.auditlog.domain.hashing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditEventHashCalculatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditEventHashCalculator calculator = new AuditEventHashCalculator(new CanonicalJson(objectMapper));

    @Test
    void equivalentPayloadKeyOrderProducesTheSameCommitment() throws Exception {
        assertThat(calculator.payloadCommitment(objectMapper.readTree("{\"b\":2,\"a\":1}")))
                .isEqualTo(calculator.payloadCommitment(objectMapper.readTree("{\"a\":1,\"b\":2}")));
    }

    @Test
    void anyImmutableFieldChangeProducesADifferentEventHash() throws Exception {
        UUID id = UUID.fromString("6e2b2b4e-59ad-46f0-87d1-2f5fac051160");
        var original = new AuditEventHashMaterial(id, "ACCOUNT_VIEWED", "advisor-1", "account", "A-100",
                Instant.parse("2026-08-18T10:15:30Z"), objectMapper.readTree("{\"reason\":\"review\"}"),
                "a".repeat(64), HashConstants.GENESIS_HASH);
        var altered = new AuditEventHashMaterial(id, "ACCOUNT_VIEWED", "advisor-2", "account", "A-100",
                original.occurredAt(), original.payloadProjection(), original.payloadCommitment(), original.previousHash());

        assertThat(calculator.eventHash(original)).isNotEqualTo(calculator.eventHash(altered));
    }
}
