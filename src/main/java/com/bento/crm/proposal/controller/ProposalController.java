package com.bento.crm.proposal.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.proposal.dto.CreateProposalRequest;
import com.bento.crm.proposal.dto.ProposalResponse;
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
    public ResponseEntity<ProposalResponse> createProposal(@Valid @RequestBody CreateProposalRequest request) {
        Proposal created = proposalService.createProposal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProposalResponse.fromEntity(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "Get proposal by ID", description = "Retrieve proposal details")
    public ResponseEntity<ProposalResponse> getProposal(@PathVariable UUID id) {
        Proposal proposal = proposalService.getProposal(id);
        return ResponseEntity.ok(ProposalResponse.fromEntity(proposal));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROPOSALS_READ')")
    @Operation(summary = "List proposals", description = "List all proposals in the organization")
    public ResponseEntity<PageResponse<ProposalResponse>> listProposals(Pageable pageable) {
        Page<Proposal> page = proposalService.listProposals(pageable);
        Page<ProposalResponse> dtoPage = page.map(ProposalResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.fromPage(dtoPage));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_WRITE')")
    @Operation(summary = "Update proposal", description = "Update proposal information")
    public ResponseEntity<ProposalResponse> updateProposal(@PathVariable UUID id, @Valid @RequestBody CreateProposalRequest request) {
        Proposal proposal = proposalService.updateProposal(id, request);
        return ResponseEntity.ok(ProposalResponse.fromEntity(proposal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPOSALS_DELETE')")
    @Operation(summary = "Delete proposal", description = "Delete proposal record")
    public ResponseEntity<Void> deleteProposal(@PathVariable UUID id) {
        proposalService.deleteProposal(id);
        return ResponseEntity.noContent().build();
    }
}
