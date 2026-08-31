package com.bento.crm.invitation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationRequest {

    @NotBlank
    @Email
    private String email;

    /**
     * Pre-assigned before the invite goes out, so the invitee's permissions are settled the
     * moment they accept rather than needing a follow-up edit.
     */
    @NotBlank
    @Pattern(regexp = "ADMIN|MANAGER|SALESPERSON|SUPPORT|VIEWER",
            message = "role must be one of: ADMIN, MANAGER, SALESPERSON, SUPPORT, VIEWER")
    private String role;

    @JsonProperty("team_id")
    private String teamId;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("job_title")
    private String jobTitle;

    @Pattern(regexp = "en|fr|ar", message = "language must be one of: en, fr, ar")
    private String language;
}
