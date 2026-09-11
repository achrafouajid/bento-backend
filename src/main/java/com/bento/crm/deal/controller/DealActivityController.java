package com.bento.crm.deal.controller;

import com.bento.crm.deal.dto.CreateDealActivityRequest;
import com.bento.crm.deal.dto.DealActivityResponse;
import com.bento.crm.deal.service.DealActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/deals/{dealId}/activities")
@RequiredArgsConstructor
@Tag(name = "Deal Activities", description = "Calls, emails, meetings, recordings, notes and follow-ups logged against a deal")
public class DealActivityController {

    private final DealActivityService dealActivityService;

    @PostMapping
    @PreAuthorize("hasAuthority('DEAL_ACTIVITIES_CREATE')")
    @Operation(summary = "Log deal activity")
    public ResponseEntity<DealActivityResponse> createActivity(@PathVariable UUID dealId, @Valid @RequestBody CreateDealActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(DealActivityResponse.fromEntity(dealActivityService.createActivity(dealId, request)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEAL_ACTIVITIES_READ')")
    @Operation(summary = "List deal activities")
    public ResponseEntity<List<DealActivityResponse>> listActivities(@PathVariable UUID dealId) {
        return ResponseEntity.ok(dealActivityService.listActivities(dealId).stream().map(DealActivityResponse::fromEntity).toList());
    }

    @PatchMapping("/{activityId}")
    @PreAuthorize("hasAuthority('DEAL_ACTIVITIES_WRITE')")
    @Operation(summary = "Update deal activity")
    public ResponseEntity<DealActivityResponse> updateActivity(@PathVariable UUID dealId, @PathVariable UUID activityId, @Valid @RequestBody CreateDealActivityRequest request) {
        return ResponseEntity.ok(DealActivityResponse.fromEntity(dealActivityService.updateActivity(dealId, activityId, request)));
    }

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasAuthority('DEAL_ACTIVITIES_DELETE')")
    @Operation(summary = "Delete deal activity")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID dealId, @PathVariable UUID activityId) {
        dealActivityService.deleteActivity(dealId, activityId);
        return ResponseEntity.noContent().build();
    }
}
