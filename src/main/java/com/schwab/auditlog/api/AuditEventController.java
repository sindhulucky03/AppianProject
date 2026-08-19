package com.schwab.auditlog.api;

import com.schwab.auditlog.api.dto.AuditEventPageResponse;
import com.schwab.auditlog.api.dto.AuditEventResponse;
import com.schwab.auditlog.api.dto.CreateAuditEventRequest;
import com.schwab.auditlog.application.AuditEventQuery;
import com.schwab.auditlog.application.AuditQueryService;
import com.schwab.auditlog.application.AuditWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/audit/events")
@Tag(name = "Audit events")
public class AuditEventController {

    private final AuditWriteService writeService;
    private final AuditQueryService queryService;

    public AuditEventController(AuditWriteService writeService, AuditQueryService queryService) {
        this.writeService = writeService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Append an audit event", description = "Adds a new immutable event to the global hash chain.")
    @ApiResponse(responseCode = "201", description = "Event appended")
    @ApiResponse(responseCode = "400", description = "Invalid event")
    public ResponseEntity<AuditEventResponse> append(@Valid @RequestBody CreateAuditEventRequest request) {
        AuditEventResponse response = writeService.append(request);
        return ResponseEntity.created(URI.create("/audit/events/" + response.eventId())).body(response);
    }

    @GetMapping
    @Operation(summary = "Query audit events", description = "Filters combine with AND. Results use ascending server timestamp and sequence order.")
    public AuditEventPageResponse query(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) String cursor) {
        return queryService.query(new AuditEventQuery(actorId, resourceType, resourceId, eventType, from, to, limit, cursor));
    }
}
