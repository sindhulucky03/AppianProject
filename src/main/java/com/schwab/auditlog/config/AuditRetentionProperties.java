package com.schwab.auditlog.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.retention")
public record AuditRetentionProperties(Duration window, Duration scheduleDelay) {
}
