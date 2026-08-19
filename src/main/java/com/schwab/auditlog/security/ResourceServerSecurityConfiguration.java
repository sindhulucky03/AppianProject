package com.schwab.auditlog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Profile("!local")
public class ResourceServerSecurityConfiguration {

    @Bean
    SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").hasAuthority("SCOPE_audit.metrics")
                        .requestMatchers(HttpMethod.POST, "/audit/events/*/redactions").hasAuthority("SCOPE_audit.admin")
                        .requestMatchers(HttpMethod.POST, "/audit/events").hasAuthority("SCOPE_audit.write")
                        .requestMatchers(HttpMethod.GET, "/audit/**").hasAuthority("SCOPE_audit.read")
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").hasAuthority("SCOPE_audit.read")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> {}))
                .build();
    }
}
