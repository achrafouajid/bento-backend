package com.bento.crm.proposal.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.proposal.model.ProposalTemplate;
import com.bento.crm.proposal.repository.ProposalTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProposalTemplateService {

    private final ProposalTemplateRepository proposalTemplateRepository;

    @Transactional
    public ProposalTemplate createTemplate(ProposalTemplate template) {
        template.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return proposalTemplateRepository.save(template);
    }

    public ProposalTemplate getTemplate(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return proposalTemplateRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal template not found"));
    }

    public Page<ProposalTemplate> listTemplates(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return proposalTemplateRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public ProposalTemplate updateTemplate(UUID id, ProposalTemplate updates) {
        ProposalTemplate template = getTemplate(id);
        template.setName(updates.getName());
        template.setSubject(updates.getSubject());
        template.setBody(updates.getBody());
        template.setChannel(updates.getChannel());
        template.setLines(updates.getLines());
        return proposalTemplateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        ProposalTemplate template = getTemplate(id);
        proposalTemplateRepository.delete(template);
    }
}
