package com.bento.crm.proposal.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.proposal.dto.ProposalTemplateRequest;
import com.bento.crm.proposal.model.ProposalTemplate;
import com.bento.crm.proposal.service.ProposalTemplateService;
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
@RequestMapping("/proposal-templates")
@Tag(name = "Proposal Templates", description = "Reusable proposal template endpoints")
public class ProposalTemplateController {

    private final ProposalTemplateService proposalTemplateService;

    public ProposalTemplateController(ProposalTemplateService proposalTemplateService) {
        this.proposalTemplateService = proposalTemplateService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPOSALS_CREATE')")
    @Operation(summary = "Create proposal template", description = "Create a new reusable proposal template")
    public ResponseEntity<ProposalTemplate> createTemplate(@Valid @RequestBody ProposalTemplateRequest request) {
        ProposalTemplate created = proposalTemplateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "Get proposal template by ID", description = "Retrieve proposal template details")
    public ResponseEntity<ProposalTemplate> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalTemplateService.getTemplate(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "List proposal templates", description = "List all proposal templates in the organization")
    public ResponseEntity<PageResponse<ProposalTemplate>> listTemplates(Pageable pageable) {
        Page<ProposalTemplate> page = proposalTemplateService.listTemplates(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_WRITE')")
    @Operation(summary = "Update proposal template", description = "Update proposal template information")
    public ResponseEntity<ProposalTemplate> updateTemplate(@PathVariable UUID id, @Valid @RequestBody ProposalTemplateRequest request) {
        return ResponseEntity.ok(proposalTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_DELETE')")
    @Operation(summary = "Delete proposal template", description = "Delete a proposal template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        proposalTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
