package com.schwab.auditlog.api;

import com.schwab.auditlog.api.dto.AuditExportFilter;
import com.schwab.auditlog.api.dto.AuditExportResponse;
import com.schwab.auditlog.api.dto.AuditSyncResponse;
import com.schwab.auditlog.application.AuditExportService;
import com.schwab.auditlog.application.AuditSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/audit")
@Tag(name = "Audit export and synchronization")
public class AuditExportController {

    private final AuditExportService exportService;
    private final AuditSyncService syncService;

    public AuditExportController(AuditExportService exportService, AuditSyncService syncService) {
        this.exportService = exportService;
        this.syncService = syncService;
    }

    @GetMapping("/export")
    @Operation(summary = "Export records with a verifiable chain witness",
            description = "Specify exactly one actorId or resourceId. Archived records are included.")
    public AuditExportResponse export(@RequestParam(required = false) String actorId,
                                      @RequestParam(required = false) String resourceId) {
        return exportService.export(new AuditExportFilter(actorId, resourceId));
    }

    @GetMapping("/events/sync")
    @Operation(summary = "Incrementally synchronize appended audit events",
            description = "Use nextAfterSequence as the watermark for the next request.")
    public AuditSyncResponse sync(@RequestParam(defaultValue = "0") @Min(0) long afterSequence,
                                  @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return syncService.sync(afterSequence, limit);
    }
}
