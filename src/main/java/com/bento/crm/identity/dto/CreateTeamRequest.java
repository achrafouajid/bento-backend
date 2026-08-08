package com.bento.crm.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotBlank
    private String name;

    private String department;

    private String description;

    @JsonProperty("lead_user_id")
    private String leadUserId;

    private String color;
}
