package com.schwab.auditlog.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.schwab.auditlog.api.AuditEventController;
import com.schwab.auditlog.application.AuditQueryService;
import com.schwab.auditlog.application.AuditWriteService;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(AuditEventController.class)
@Import({ResourceServerSecurityConfiguration.class, ResourceServerSecurityConfigurationTest.TestConfig.class})
class ResourceServerSecurityConfigurationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuditWriteService writeService;
    @MockitoBean private AuditQueryService queryService;

    @Test
    void rejectsUnauthenticatedAuditReads() throws Exception {
        mockMvc.perform(get("/audit/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAuditReadsWithoutTheReadScope() throws Exception {
        mockMvc.perform(get("/audit/events").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_audit.write"))))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> { throw new UnsupportedOperationException("MockMvc injects the JWT authentication directly"); };
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }
}
