package com.bento.crm.partner.controller;

import com.bento.crm.partner.dto.PartnerLedgerResponse;
import com.bento.crm.partner.service.PartnerLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/partners/{partnerId}/ledger")
@RequiredArgsConstructor
@Tag(name = "Partner ledger", description = "Open invoices, closed invoices and the signed transaction ledger for one partner")
public class PartnerLedgerController {

    private final PartnerLedgerService partnerLedgerService;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTNERS_READ')")
    @Operation(summary = "Get partner ledger",
            description = "Customer amounts are receivable (invoice positive, payment negative); vendor amounts are payable and inverted")
    public ResponseEntity<PartnerLedgerResponse> getLedger(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(partnerLedgerService.getLedger(partnerId));
    }
}
