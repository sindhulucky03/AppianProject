package com.schwab.auditlog.application;

import com.schwab.auditlog.config.AuditRetentionProperties;
import com.schwab.auditlog.persistence.repository.AuditEventLifecycleRepository;
import java.time.Clock;
import java.time.Instant;
import com.schwab.auditlog.observability.AuditMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetentionService {

    private final AuditEventLifecycleRepository lifecycleRepository;
    private final AuditRetentionProperties retentionProperties;
    private final Clock clock;
    private final AuditMetrics metrics;

    public RetentionService(AuditEventLifecycleRepository lifecycleRepository, AuditRetentionProperties retentionProperties,
                            Clock clock, AuditMetrics metrics) {
        this.lifecycleRepository = lifecycleRepository;
        this.retentionProperties = retentionProperties;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Transactional
    public int archiveExpiredEvents() {
        Instant archivedAt = clock.instant();
        int archived = lifecycleRepository.archiveBefore(archivedAt.minus(retentionProperties.window()), archivedAt);
        metrics.eventsArchived(archived);
        return archived;
    }
}
