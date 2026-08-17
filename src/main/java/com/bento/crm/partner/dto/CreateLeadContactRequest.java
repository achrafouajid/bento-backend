package com.bento.crm.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadContactRequest {

    @NotBlank
    private String name;

    @JsonProperty("job_title")
    private String jobTitle;

    private String email;

    private String phone;

    private String mobile;

    private String website;

    private String linkedin;
}
