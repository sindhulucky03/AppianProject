package com.schwab.auditlog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.auditlog.api.dto.ChainViolationType;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.application.AuditWriteService;
import com.schwab.auditlog.application.ChainVerificationService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("local")
class ChainVerificationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private AuditWriteService writeService;
    @Autowired
    private ChainVerificationService verificationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void detectsARecordModifiedDirectlyInTheDataStore() throws Exception {
        writeService.append(event("advisor-1"));
        var second = writeService.append(event("advisor-2"));

        jdbcTemplate.execute("ALTER TABLE audit_event DISABLE TRIGGER trg_prevent_audit_event_mutation");
        try {
            jdbcTemplate.update("UPDATE audit_event SET actor_id = ? WHERE event_id = ?", "tampered", second.eventId());
        } finally {
            jdbcTemplate.execute("ALTER TABLE audit_event ENABLE TRIGGER trg_prevent_audit_event_mutation");
        }

        var result = verificationService.verify();

        assertThat(result.intact()).isFalse();
        assertThat(result.firstInconsistency().sequenceNumber()).isEqualTo(second.sequenceNumber());
        assertThat(result.firstInconsistency().violation()).isEqualTo(ChainViolationType.CONTENT_HASH_MISMATCH);
    }

    @Test
    void verifiesAnUntamperedChain() throws Exception {
        writeService.append(event("advisor-3"));

        var result = verificationService.verify();

        assertThat(result.intact()).isTrue();
        assertThat(result.firstInconsistency()).isNull();
    }

    private CreateAuditEventRequest event(String actorId) throws Exception {
        return new CreateAuditEventRequest("ACCOUNT_VIEWED", actorId, "account", UUID.randomUUID().toString(),
                objectMapper.readTree("{\"reason\":\"review\"}"));
    }

    @AfterEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE audit_event RESTART IDENTITY CASCADE");
        jdbcTemplate.update("UPDATE audit_chain_state SET last_sequence = 0, last_hash = repeat('0', 64), updated_at = CURRENT_TIMESTAMP WHERE chain_id = 1");
    }
}
