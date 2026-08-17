package com.bento.crm.partner.controller;

import com.bento.crm.partner.dto.CreateLeadActivityRequest;
import com.bento.crm.partner.dto.CreateLeadContactRequest;
import com.bento.crm.partner.dto.CreateLeadStatusHistoryRequest;
import com.bento.crm.partner.dto.LeadActivityResponse;
import com.bento.crm.partner.dto.LeadContactResponse;
import com.bento.crm.partner.dto.LeadStatusHistoryResponse;
import com.bento.crm.partner.service.LeadSubResourceService;
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
@RequestMapping("/partners/{partnerId}")
@RequiredArgsConstructor
@Tag(name = "Lead sub-resources", description = "Contacts, activities and status history nested under a partner/lead")
public class LeadSubResourceController {

    private final LeadSubResourceService leadSubResourceService;

    @PostMapping("/contacts")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Add lead contact")
    public ResponseEntity<LeadContactResponse> addContact(@PathVariable UUID partnerId, @Valid @RequestBody CreateLeadContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadContactResponse.fromEntity(leadSubResourceService.addContact(partnerId, request)));
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAuthority('PARTNERS_READ')")
    @Operation(summary = "List lead contacts")
    public ResponseEntity<List<LeadContactResponse>> listContacts(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(leadSubResourceService.listContacts(partnerId).stream().map(LeadContactResponse::fromEntity).toList());
    }

    @PatchMapping("/contacts/{contactId}")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Update lead contact")
    public ResponseEntity<LeadContactResponse> updateContact(@PathVariable UUID partnerId, @PathVariable UUID contactId, @Valid @RequestBody CreateLeadContactRequest request) {
        return ResponseEntity.ok(LeadContactResponse.fromEntity(leadSubResourceService.updateContact(partnerId, contactId, request)));
    }

    @DeleteMapping("/contacts/{contactId}")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Delete lead contact")
    public ResponseEntity<Void> deleteContact(@PathVariable UUID partnerId, @PathVariable UUID contactId) {
        leadSubResourceService.deleteContact(partnerId, contactId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/activities")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Log lead activity")
    public ResponseEntity<LeadActivityResponse> addActivity(@PathVariable UUID partnerId, @Valid @RequestBody CreateLeadActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadActivityResponse.fromEntity(leadSubResourceService.addActivity(partnerId, request)));
    }

    @GetMapping("/activities")
    @PreAuthorize("hasAuthority('PARTNERS_READ')")
    @Operation(summary = "List lead activities")
    public ResponseEntity<List<LeadActivityResponse>> listActivities(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(leadSubResourceService.listActivities(partnerId).stream().map(LeadActivityResponse::fromEntity).toList());
    }

    @DeleteMapping("/activities/{activityId}")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Delete lead activity")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID partnerId, @PathVariable UUID activityId) {
        leadSubResourceService.deleteActivity(partnerId, activityId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/status-history")
    @PreAuthorize("hasAuthority('PARTNERS_WRITE')")
    @Operation(summary = "Record lead status change")
    public ResponseEntity<LeadStatusHistoryResponse> addStatusHistory(@PathVariable UUID partnerId, @Valid @RequestBody CreateLeadStatusHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadStatusHistoryResponse.fromEntity(leadSubResourceService.addStatusHistory(partnerId, request)));
    }

    @GetMapping("/status-history")
    @PreAuthorize("hasAuthority('PARTNERS_READ')")
    @Operation(summary = "List lead status history")
    public ResponseEntity<List<LeadStatusHistoryResponse>> listStatusHistory(@PathVariable UUID partnerId) {
        return ResponseEntity.ok(leadSubResourceService.listStatusHistory(partnerId).stream().map(LeadStatusHistoryResponse::fromEntity).toList());
    }
}
