package com.schwab.auditlog.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetentionScheduler {

    private final RetentionService retentionService;

    public RetentionScheduler(RetentionService retentionService) {
        this.retentionService = retentionService;
    }

    @Scheduled(fixedDelayString = "${audit.retention.schedule-delay}")
    public void archiveExpiredEvents() {
        retentionService.archiveExpiredEvents();
    }
}
