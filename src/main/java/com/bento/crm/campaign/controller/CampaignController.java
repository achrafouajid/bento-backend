package com.bento.crm.campaign.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.campaign.model.Campaign;
import com.bento.crm.campaign.service.CampaignService;
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
@RequestMapping("/campaigns")
@Tag(name = "Campaigns", description = "Campaign management endpoints")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CAMPAIGNS_CREATE')")
    @Operation(summary = "Create campaign", description = "Create a new campaign")
    public ResponseEntity<Campaign> createCampaign(@Valid @RequestBody Campaign campaign) {
        Campaign created = campaignService.createCampaign(campaign);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CAMPAIGNS_READ')")
    @Operation(summary = "Get campaign by ID", description = "Retrieve campaign details")
    public ResponseEntity<Campaign> getCampaign(@PathVariable UUID id) {
        Campaign campaign = campaignService.getCampaign(id);
        return ResponseEntity.ok(campaign);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CAMPAIGNS_READ')")
    @Operation(summary = "List campaigns", description = "List all campaigns in the organization")
    public ResponseEntity<PageResponse<Campaign>> listCampaigns(Pageable pageable) {
        Page<Campaign> page = campaignService.listCampaigns(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Update campaign", description = "Update campaign information")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable UUID id, @Valid @RequestBody Campaign updates) {
        Campaign campaign = campaignService.updateCampaign(id, updates);
        return ResponseEntity.ok(campaign);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CAMPAIGNS_DELETE')")
    @Operation(summary = "Delete campaign", description = "Delete campaign record")
    public ResponseEntity<Void> deleteCampaign(@PathVariable UUID id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}
