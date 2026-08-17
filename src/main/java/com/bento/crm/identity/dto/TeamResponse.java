package com.bento.crm.identity.dto;

import com.bento.crm.identity.model.Team;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private UUID id;

    @JsonProperty("organization_id")
    private UUID organizationId;

    private String name;

    private Team.TeamDepartment department;

    private String description;

    @JsonProperty("lead_user_id")
    private UUID leadUserId;

    private String color;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .organizationId(team.getOrganizationId())
                .name(team.getName())
                .department(team.getDepartment())
                .description(team.getDescription())
                .leadUserId(team.getLeadUserId())
                .color(team.getColor())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
