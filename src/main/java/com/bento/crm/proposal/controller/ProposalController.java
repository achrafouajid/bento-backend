package com.bento.crm.proposal.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.proposal.model.Proposal;
import com.bento.crm.proposal.service.ProposalService;
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
@RequestMapping("/proposals")
@Tag(name = "Proposals", description = "Proposal management endpoints")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPOSALS_CREATE')")
    @Operation(summary = "Create proposal", description = "Create a new proposal")
    public ResponseEntity<Proposal> createProposal(@Valid @RequestBody Proposal proposal) {
        Proposal created = proposalService.createProposal(proposal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "Get proposal by ID", description = "Retrieve proposal details")
    public ResponseEntity<Proposal> getProposal(@PathVariable UUID id) {
        Proposal proposal = proposalService.getProposal(id);
        return ResponseEntity.ok(proposal);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "List proposals", description = "List all proposals in the organization")
    public ResponseEntity<PageResponse<Proposal>> listProposals(Pageable pageable) {
        Page<Proposal> page = proposalService.listProposals(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_WRITE')")
    @Operation(summary = "Update proposal", description = "Update proposal information")
    public ResponseEntity<Proposal> updateProposal(@PathVariable UUID id, @Valid @RequestBody Proposal updates) {
        Proposal proposal = proposalService.updateProposal(id, updates);
        return ResponseEntity.ok(proposal);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_DELETE')")
    @Operation(summary = "Delete proposal", description = "Delete proposal record")
    public ResponseEntity<Void> deleteProposal(@PathVariable UUID id) {
        proposalService.deleteProposal(id);
        return ResponseEntity.noContent().build();
    }
}
