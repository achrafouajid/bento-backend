package com.bento.crm.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartnerRequest {

    @NotBlank
    private String type;

    @NotBlank
    private String name;

    @JsonProperty("company_name")
    private String companyName;

    private String email;

    private String phone;

    private String city;

    private String country;

    private String source;

    private Integer score;

    private String temperature;

    private String priority;

    private String qualification;

    private String stage;

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("estimated_deal_value")
    private BigDecimal estimatedDealValue;

    private Integer probability;

    private String comments;
}
