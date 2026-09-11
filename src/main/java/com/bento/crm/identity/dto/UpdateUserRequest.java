package com.bento.crm.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @JsonProperty("display_name")
    private String displayName;

    private String role;

    @JsonProperty("team_id")
    private String teamId;

    private String phone;

    @JsonProperty("job_title")
    private String jobTitle;

    @Pattern(regexp = "en|fr|ar", message = "language must be one of: en, fr, ar")
    private String language;
}
