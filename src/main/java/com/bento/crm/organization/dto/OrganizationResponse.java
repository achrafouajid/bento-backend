package com.bento.crm.organization.dto;

import com.bento.crm.organization.model.Organization;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

    private UUID id;

    private String name;

    @JsonProperty("logo_url")
    private String logoUrl;

    private String industry;

    private String timezone;

    @JsonProperty("fiscal_year_start_month")
    private Integer fiscalYearStartMonth;

    @JsonProperty("default_currency")
    private String defaultCurrency;

    private String plan;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static OrganizationResponse fromEntity(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .logoUrl(organization.getLogoUrl())
                .industry(organization.getIndustry())
                .timezone(organization.getTimezone())
                .fiscalYearStartMonth(organization.getFiscalYearStartMonth())
                .defaultCurrency(organization.getDefaultCurrency())
                .plan(organization.getPlan())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }
}
