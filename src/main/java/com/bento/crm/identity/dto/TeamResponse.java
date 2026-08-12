package com.bento.crm.identity.dto;

import com.bento.crm.identity.model.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private Team.TeamDepartment department;
    private String description;
    private UUID leadUserId;
    private String color;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .organizationId(team.getOrganizationId())
                .name(team.getName())
                .department(team.getDepartment())
                .description(team.getDescription())
                .leadUserId(team.getLeadUserId())
                .color(team.getColor())
                .createdBy(team.getCreatedBy())
                .updatedBy(team.getUpdatedBy())
                .createdAt(team.getCreatedAt() != null ?
                    LocalDateTime.from(team.getCreatedAt()) : null)
                .updatedAt(team.getUpdatedAt() != null ?
                    LocalDateTime.from(team.getUpdatedAt()) : null)
                .build();
    }
}
