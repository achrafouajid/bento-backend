package com.bento.crm.proposal.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.proposal.model.Proposal;
import com.bento.crm.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;

    @Transactional
    public Proposal createProposal(Proposal proposal) {
        proposal.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return proposalRepository.save(proposal);
    }

    public Proposal getProposal(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return proposalRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
    }

    public Page<Proposal> listProposals(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return proposalRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public Proposal updateProposal(UUID id, Proposal updates) {
        Proposal proposal = getProposal(id);
        proposal.setTitle(updates.getTitle());
        proposal.setStatus(updates.getStatus());
        proposal.setOpportunityValue(updates.getOpportunityValue());
        return proposalRepository.save(proposal);
    }

    @Transactional
    public void deleteProposal(UUID id) {
        Proposal proposal = getProposal(id);
        proposalRepository.delete(proposal);
    }
}
