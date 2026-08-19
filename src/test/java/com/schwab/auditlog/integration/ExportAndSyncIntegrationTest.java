package com.schwab.auditlog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.application.AuditExportService;
import com.schwab.auditlog.application.AuditSyncService;
import com.schwab.auditlog.application.AuditWriteService;
import com.schwab.auditlog.application.ExportBundleVerifier;
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
class ExportAndSyncIntegrationTest {

    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired private AuditWriteService writeService;
    @Autowired private AuditExportService exportService;
    @Autowired private AuditSyncService syncService;
    @Autowired private ExportBundleVerifier bundleVerifier;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void exportsSelectedEventsWithAnIndependentlyVerifiableWitness() throws Exception {
        write("advisor-1", "A-100");
        write("advisor-2", "A-200");

        var bundle = exportService.export(new com.schwab.auditlog.api.dto.AuditExportFilter("advisor-1", null));

        assertThat(bundle.events()).hasSize(1);
        assertThat(bundle.chainWitness()).hasSize(2);
        assertThat(bundleVerifier.verify(bundle).intact()).isTrue();
    }

    @Test
    void advancesTheIncrementalWatermarkWithoutDuplicates() throws Exception {
        var first = write("advisor-1", "A-100");
        var second = write("advisor-2", "A-200");

        var sync = syncService.sync(first.sequenceNumber(), 100);

        assertThat(sync.events()).extracting(event -> event.eventId()).containsExactly(second.eventId());
        assertThat(sync.nextAfterSequence()).isEqualTo(second.sequenceNumber());
        assertThat(sync.hasMore()).isFalse();
    }

    private com.schwab.auditlog.api.dto.AuditEventResponse write(String actorId, String resourceId) throws Exception {
        return writeService.append(new CreateAuditEventRequest("ACCOUNT_VIEWED", actorId, "account", resourceId,
                objectMapper.readTree("{\"reason\":\"review\"}")));
    }

    @AfterEach void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE audit_event RESTART IDENTITY CASCADE");
        jdbcTemplate.update("UPDATE audit_chain_state SET last_sequence = 0, last_hash = repeat('0', 64), updated_at = CURRENT_TIMESTAMP WHERE chain_id = 1");
    }
}
