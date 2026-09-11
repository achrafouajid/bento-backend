package com.bento.crm.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCardRequest {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("record_type")
    private String recordType;

    @NotBlank
    private String name;

    @JsonProperty("search_name")
    private String searchName;

    @JsonProperty("erp_account")
    private String erpAccount;

    @NotBlank
    private String ice;

    @NotBlank
    @JsonProperty("if_field")
    private String ifField;

    @NotBlank
    private String rc;

    @JsonProperty("rc_city")
    private String rcCity;

    private String tp;

    @JsonProperty("vat_status")
    private List<String> vatStatus;

    @JsonProperty("org_type")
    private String orgType;

    @JsonProperty("parent_account_id")
    private String parentAccountId;

    private List<Map<String, Object>> addresses;

    @JsonProperty("main_phone")
    private String mainPhone;

    @JsonProperty("corporate_email")
    private String corporateEmail;

    @JsonProperty("website_url")
    private String websiteUrl;

    private List<Map<String, Object>> personnel;
}
