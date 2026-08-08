package com.bento.crm.identity.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.identity.dto.CreateTeamRequest;
import com.bento.crm.identity.model.CrmGroup;
import com.bento.crm.identity.repository.CrmGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrmGroupService {

    private final CrmGroupRepository crmGroupRepository;

    public CrmGroupService(CrmGroupRepository crmGroupRepository) {
        this.crmGroupRepository = crmGroupRepository;
    }

    @Transactional
    public CrmGroup createTeam(CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        CrmGroup group = CrmGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        group.setOrganizationId(orgId);
        return crmGroupRepository.save(group);
    }

    public CrmGroup getTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    public Page<CrmGroup> listTeams(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return crmGroupRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public CrmGroup updateTeam(UUID teamId, CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        CrmGroup group = crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());

        return crmGroupRepository.save(group);
    }

    @Transactional
    public void deleteTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        CrmGroup group = crmGroupRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        crmGroupRepository.delete(group);
    }
}
