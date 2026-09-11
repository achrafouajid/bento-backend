package com.bento.crm.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private UUID id;

    @JsonProperty("organization_id")
    private UUID organizationId;

    private String email;

    @JsonProperty("display_name")
    private String displayName;

    private String initials;

    @JsonProperty("avatar_color")
    private String avatarColor;

    private String role;

    @JsonProperty("team_id")
    private UUID teamId;

    @JsonProperty("is_active")
    private Boolean isActive;

    private String phone;

    @JsonProperty("job_title")
    private String jobTitle;

    private String language;

    @JsonProperty("last_active_at")
    private String lastActiveAt;
}
