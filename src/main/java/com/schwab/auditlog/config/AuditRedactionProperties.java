package com.schwab.auditlog.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.redaction")
public record AuditRedactionProperties(Set<String> sensitiveFieldNames, String masterKey) {
}
