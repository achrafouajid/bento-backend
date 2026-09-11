package com.bento.crm.deal.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.deal.dto.CreateDealRequest;
import com.bento.crm.deal.dto.DealResponse;
import com.bento.crm.deal.model.Deal;
import com.bento.crm.deal.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/deals")
@Tag(name = "Deals", description = "Deal management endpoints")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEALS_CREATE')")
    @Operation(summary = "Create deal", description = "Create a new deal")
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody CreateDealRequest request) {
        Deal created = dealService.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DealResponse.fromEntity(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEALS_READ')")
    @Operation(summary = "Get deal by ID", description = "Retrieve deal details")
    public ResponseEntity<DealResponse> getDeal(@PathVariable UUID id) {
        Deal deal = dealService.getDeal(id);
        return ResponseEntity.ok(DealResponse.fromEntity(deal));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEALS_READ')")
    @Operation(summary = "List deals", description = "List all deals in the organization")
    public ResponseEntity<PageResponse<DealResponse>> listDeals(Pageable pageable) {
        Page<Deal> page = dealService.listDeals(pageable);
        Page<DealResponse> dtoPage = page.map(DealResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.fromPage(dtoPage));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('DEALS_WRITE')")
    @Operation(summary = "Update deal", description = "Update deal information")
    public ResponseEntity<DealResponse> updateDeal(@PathVariable UUID id, @Valid @RequestBody CreateDealRequest request) {
        Deal deal = dealService.updateDeal(id, request);
        return ResponseEntity.ok(DealResponse.fromEntity(deal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEALS_DELETE')")
    @Operation(summary = "Delete deal", description = "Delete deal record")
    public ResponseEntity<Void> deleteDeal(@PathVariable UUID id) {
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }
}
