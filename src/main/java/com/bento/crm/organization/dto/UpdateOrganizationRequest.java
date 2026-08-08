package com.bento.crm.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String industry;

    @JsonProperty("logo_url")
    private String logoUrl;

    private String timezone;

    @JsonProperty("default_currency")
    private String defaultCurrency;

    @JsonProperty("fiscal_year_start_month")
    private Integer fiscalYearStartMonth;
}
