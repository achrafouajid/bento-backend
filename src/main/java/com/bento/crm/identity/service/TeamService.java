package com.bento.crm.identity.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.identity.dto.CreateTeamRequest;
import com.bento.crm.identity.model.Team;
import com.bento.crm.identity.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Team createTeam(CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        team.setOrganizationId(orgId);
        return teamRepository.save(team);
    }

    public Team getTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return teamRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    public Page<Team> listTeams(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return teamRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public Team updateTeam(UUID teamId, CreateTeamRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Team team = teamRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(UUID teamId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Team team = teamRepository.findByOrganizationIdAndId(orgId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        teamRepository.delete(team);
    }
}
