package com.bento.crm.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String industry;

    @JsonProperty("default_currency")
    private String defaultCurrency;

    private String timezone;

    @JsonProperty("admin_email")
    @NotBlank(message = "Admin email is required")
    private String adminEmail;

    @JsonProperty("admin_name")
    @NotBlank(message = "Admin name is required")
    private String adminName;

    @JsonProperty("admin_password")
    @NotBlank(message = "Admin password is required")
    private String adminPassword;
}
