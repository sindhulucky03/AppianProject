package com.schwab.auditlog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.auditlog.api.dto.AuditEventPageResponse;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.api.dto.RedactPayloadRequest;
import com.schwab.auditlog.application.AuditEventQuery;
import com.schwab.auditlog.application.AuditQueryService;
import com.schwab.auditlog.application.AuditWriteService;
import com.schwab.auditlog.application.ChainVerificationService;
import com.schwab.auditlog.application.RedactionService;
import com.schwab.auditlog.application.RetentionService;
import java.util.List;
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
class RetentionAndRedactionIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("audit.retention.window", () -> "PT0S");
    }

    @Autowired private AuditWriteService writeService;
    @Autowired private AuditQueryService queryService;
    @Autowired private ChainVerificationService verificationService;
    @Autowired private RedactionService redactionService;
    @Autowired private RetentionService retentionService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void redactionDestroysThePerFieldKeyWithoutBreakingTheChain() throws Exception {
        var event = writeService.append(new CreateAuditEventRequest("ACCOUNT_VIEWED", "advisor-1", "account", "A-100",
                objectMapper.readTree("{\"email\":\"client@example.test\",\"reason\":\"review\"}")));

        assertThat(event.payload().get("email").asText()).isEqualTo("[REDACTED]");
        redactionService.redact(event.eventId(), new RedactPayloadRequest(List.of("/email")));

        assertThat(jdbcTemplate.queryForObject("SELECT encrypted_data_key IS NULL FROM audit_sensitive_payload WHERE event_id = ? AND json_pointer = '/email'", Boolean.class, event.eventId())).isTrue();
        assertThat(verificationService.verify().intact()).isTrue();
    }

    @Test
    void archivedEventsAreExcludedFromQueriesButRemainChainVerifiable() throws Exception {
        writeService.append(new CreateAuditEventRequest("ACCOUNT_VIEWED", "advisor-2", "account", "A-200",
                objectMapper.readTree("{\"reason\":\"review\"}")));

        assertThat(retentionService.archiveExpiredEvents()).isEqualTo(1);
        AuditEventPageResponse result = queryService.query(new AuditEventQuery(null, null, null, null, null, null, 50, null));

        assertThat(result.events()).isEmpty();
        assertThat(verificationService.verify().intact()).isTrue();
    }

    @AfterEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE audit_event RESTART IDENTITY CASCADE");
        jdbcTemplate.update("UPDATE audit_chain_state SET last_sequence = 0, last_hash = repeat('0', 64), updated_at = CURRENT_TIMESTAMP WHERE chain_id = 1");
    }
}
