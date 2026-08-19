package com.schwab.auditlog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.application.AuditWriteService;
import com.schwab.auditlog.application.ChainVerificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
class ConcurrentAppendIntegrationTest {

    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired private AuditWriteService writeService;
    @Autowired private ChainVerificationService verificationService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void preservesAValidChainUnderConcurrentAppends() throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int index = 0; index < 32; index++) {
                int taskNumber = index;
                tasks.add(() -> {
                    writeService.append(new CreateAuditEventRequest("ACCOUNT_VIEWED", "advisor-" + taskNumber,
                            "account", "A-" + taskNumber, objectMapper.readTree("{\"reason\":\"benchmark\"}")));
                    return null;
                });
            }
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        }

        var verification = verificationService.verify();
        assertThat(verification.intact()).isTrue();
        assertThat(verification.recordCount()).isEqualTo(32);
    }

    @AfterEach void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE audit_event RESTART IDENTITY CASCADE");
        jdbcTemplate.update("UPDATE audit_chain_state SET last_sequence = 0, last_hash = repeat('0', 64), updated_at = CURRENT_TIMESTAMP WHERE chain_id = 1");
    }
}
