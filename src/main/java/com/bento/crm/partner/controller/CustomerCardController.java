package com.bento.crm.partner.controller;

import com.bento.crm.partner.dto.CustomerCardRequest;
import com.bento.crm.partner.dto.CustomerCardResponse;
import com.bento.crm.partner.service.CustomerCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/partners/{partnerId}/customer-card")
@RequiredArgsConstructor
@Tag(name = "Customer card", description = "Moroccan legal/fiscal business-registration record nested under a partner")
public class CustomerCardController {

    private final CustomerCardService customerCardService;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTNERS_READ')")
    @Operation(summary = "Get customer card")
    public ResponseEntity<CustomerCardResponse> getCard(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(CustomerCardResponse.fromEntity(customerCardService.getCard(partnerId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Create or update customer card")
    public ResponseEntity<CustomerCardResponse> saveCard(@PathVariable UUID partnerId, @Valid @RequestBody CustomerCardRequest request) {
        return ResponseEntity.ok(CustomerCardResponse.fromEntity(customerCardService.saveCard(partnerId, request)));
    }
}
