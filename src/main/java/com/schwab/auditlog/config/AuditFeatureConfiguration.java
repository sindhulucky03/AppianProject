package com.schwab.auditlog.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({AuditRetentionProperties.class, AuditRedactionProperties.class})
public class AuditFeatureConfiguration {
}
