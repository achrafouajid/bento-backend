package com.bento.crm.identity.dto;

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
public class CreateUserRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @JsonProperty("display_name")
    private String displayName;

    @NotBlank
    private String password;

    private String role;

    @JsonProperty("team_id")
    private String teamId;

    private String phone;

    @JsonProperty("job_title")
    private String jobTitle;

    @Pattern(regexp = "en|fr|ar", message = "language must be one of: en, fr, ar")
    private String language;
}
