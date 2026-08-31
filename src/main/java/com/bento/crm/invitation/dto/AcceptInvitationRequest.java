package com.bento.crm.invitation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationRequest {

    @NotBlank
    private String token;

    @NotBlank
    @JsonProperty("display_name")
    private String displayName;

    /**
     * Chosen by the invitee and never transmitted to them, which is the whole point of the
     * invitation flow: no password is ever generated on their behalf.
     */
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;
}
