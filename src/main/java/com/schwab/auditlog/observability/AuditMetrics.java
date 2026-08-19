package com.schwab.auditlog.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AuditMetrics {

    private final Counter appended;
    private final Counter redacted;
    private final Counter archived;
    private final Counter exported;
    private final Timer appendLatency;
    private final Timer queryLatency;
    private final Timer verificationLatency;
    private final Timer exportLatency;
    private final Timer syncLatency;

    public AuditMetrics(MeterRegistry registry) {
        appended = registry.counter("audit.events.appended");
        redacted = registry.counter("audit.payloads.redacted");
        archived = registry.counter("audit.events.archived");
        exported = registry.counter("audit.exports.created");
        appendLatency = registry.timer("audit.append.duration");
        queryLatency = registry.timer("audit.query.duration");
        verificationLatency = registry.timer("audit.verification.duration");
        exportLatency = registry.timer("audit.export.duration");
        syncLatency = registry.timer("audit.sync.duration");
    }

    public void eventAppended() { appended.increment(); }
    public void payloadsRedacted(int count) { redacted.increment(count); }
    public void eventsArchived(int count) { archived.increment(count); }
    public void exportCreated() { exported.increment(); }
    public void appendCompleted(long nanoseconds) { appendLatency.record(nanoseconds, java.util.concurrent.TimeUnit.NANOSECONDS); }
    public void queryCompleted(long nanoseconds) { queryLatency.record(nanoseconds, java.util.concurrent.TimeUnit.NANOSECONDS); }
    public void verificationCompleted(long nanoseconds) { verificationLatency.record(nanoseconds, java.util.concurrent.TimeUnit.NANOSECONDS); }
    public void exportCompleted(long nanoseconds) { exportLatency.record(nanoseconds, java.util.concurrent.TimeUnit.NANOSECONDS); }
    public void syncCompleted(long nanoseconds) { syncLatency.record(nanoseconds, java.util.concurrent.TimeUnit.NANOSECONDS); }
}
