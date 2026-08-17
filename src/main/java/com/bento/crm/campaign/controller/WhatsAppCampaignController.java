package com.bento.crm.campaign.controller;

import com.bento.crm.campaign.dto.CampaignRecipientResponse;
import com.bento.crm.campaign.dto.CampaignResponse;
import com.bento.crm.campaign.dto.CampaignStatsResponse;
import com.bento.crm.campaign.dto.WhatsAppCampaignRequest;
import com.bento.crm.campaign.model.Campaign;
import com.bento.crm.campaign.service.CampaignLaunchService;
import com.bento.crm.campaign.service.CampaignRecipientService;
import com.bento.crm.common.context.TenantContext;
import com.bento.crm.whatsapp.service.WaFollowupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WhatsApp-specific campaign operations for the /marketing route.
 */
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Campaigns", description = "Send and track WhatsApp campaigns")
public class WhatsAppCampaignController {

    private final CampaignLaunchService launchService;
    private final CampaignRecipientService recipientService;
    private final WaFollowupService followupService;

    @PostMapping("/whatsapp")
    @PreAuthorize("hasAuthority('CAMPAIGNS_CREATE')")
    @Operation(summary = "Create a WhatsApp campaign for the selected contacts")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody WhatsAppCampaignRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Campaign campaign = launchService.createWhatsAppCampaign(orgId, request);

        if (request.isLaunchNow()) {
            campaign = launchService.launch(orgId, campaign.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(CampaignResponse.fromEntity(campaign));
    }

    @PostMapping("/{id}/launch")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Send the campaign to every pending recipient")
    public ResponseEntity<CampaignResponse> launch(@PathVariable UUID id) {
        Campaign campaign = launchService.launch(TenantContext.getCurrentOrganizationId(), id);
        return ResponseEntity.accepted().body(CampaignResponse.fromEntity(campaign));
    }

    @PostMapping("/{id}/recipients")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Add more contacts to an existing campaign")
    public ResponseEntity<List<CampaignRecipientResponse>> addRecipients(@PathVariable UUID id,
                                                                        @RequestBody Map<String, List<UUID>> body) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        launchService.addRecipients(orgId, id, body.getOrDefault("partnerIds", List.of()));
        return ResponseEntity.ok(recipientService.listRecipients(orgId, id));
    }

    @GetMapping("/{id}/recipients")
    @PreAuthorize("hasAuthority('CAMPAIGNS_READ')")
    @Operation(summary = "Per-recipient delivery and reply status")
    public ResponseEntity<List<CampaignRecipientResponse>> recipients(@PathVariable UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return ResponseEntity.ok(recipientService.listRecipients(orgId, id));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAuthority('CAMPAIGNS_READ')")
    @Operation(summary = "Live campaign counters")
    public ResponseEntity<CampaignStatsResponse> stats(@PathVariable UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return ResponseEntity.ok(recipientService.stats(orgId, id));
    }

    /**
     * Stops queued relances without touching messages already sent — the "we closed
     * this deal by phone, stop chasing them" action.
     */
    @PostMapping("/{id}/cancel-followups")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Cancel all pending relances for this campaign")
    public ResponseEntity<Map<String, Integer>> cancelFollowups(@PathVariable UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        int cancelled = followupService.cancelForCampaign(orgId, id);
        return ResponseEntity.ok(Map.of("cancelled", cancelled));
    }
}
