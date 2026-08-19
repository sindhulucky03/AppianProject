package com.schwab.auditlog.api;

import com.schwab.auditlog.api.dto.ChainVerificationResponse;
import com.schwab.auditlog.application.ChainVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/verify")
@Tag(name = "Audit integrity")
public class AuditVerificationController {

    private final ChainVerificationService verificationService;

    public AuditVerificationController(ChainVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    @Operation(summary = "Verify the complete audit hash chain",
            description = "Reports the first detected content, predecessor, or chain-head inconsistency.")
    public ChainVerificationResponse verify() {
        return verificationService.verify();
    }
}
