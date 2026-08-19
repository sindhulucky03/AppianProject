package com.schwab.auditlog.api;

import com.schwab.auditlog.api.dto.RedactPayloadRequest;
import com.schwab.auditlog.api.dto.RedactionResponse;
import com.schwab.auditlog.application.RedactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/events")
@Tag(name = "Audit privacy")
public class AuditRedactionController {

    private final RedactionService redactionService;

    public AuditRedactionController(RedactionService redactionService) {
        this.redactionService = redactionService;
    }

    @PostMapping("/{eventId}/redactions")
    @Operation(summary = "Cryptographically redact sensitive payload fields",
            description = "Destroys per-field data keys without changing the immutable audit event or its hash.")
    public RedactionResponse redact(@PathVariable UUID eventId, @Valid @RequestBody RedactPayloadRequest request) {
        return redactionService.redact(eventId, request);
    }
}
