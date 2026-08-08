package com.bento.crm.identity.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.identity.dto.CreateTeamRequest;
import com.bento.crm.identity.model.Team;
import com.bento.crm.identity.service.TeamService;
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
@RequestMapping("/teams")
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TEAMS_CREATE')")
    @Operation(summary = "Create team", description = "Create new team in the organization")
    public ResponseEntity<Team> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        Team team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAMS_READ')")
    @Operation(summary = "Get team by ID", description = "Retrieve team details")
    public ResponseEntity<Team> getTeam(@PathVariable UUID id) {
        Team team = teamService.getTeam(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEAMS_READ')")
    @Operation(summary = "List teams", description = "List all teams in the organization")
    public ResponseEntity<PageResponse<Team>> listTeams(Pageable pageable) {
        Page<Team> page = teamService.listTeams(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAMS_WRITE')")
    @Operation(summary = "Update team", description = "Update team information")
    public ResponseEntity<Team> updateTeam(@PathVariable UUID id, @Valid @RequestBody CreateTeamRequest request) {
        Team team = teamService.updateTeam(id, request);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAMS_DELETE')")
    @Operation(summary = "Delete team", description = "Delete team record")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
