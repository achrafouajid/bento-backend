package com.bento.crm.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String language;
}
