package com.bento.crm.partner.dto;

import com.bento.crm.partner.model.CustomerCard;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCardResponse {

    private UUID id;

    @JsonProperty("partner_id")
    private UUID partnerId;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("record_type")
    private String recordType;

    private String name;

    @JsonProperty("search_name")
    private String searchName;

    @JsonProperty("erp_account")
    private String erpAccount;

    private String ice;

    @JsonProperty("if_field")
    private String ifField;

    private String rc;

    @JsonProperty("rc_city")
    private String rcCity;

    private String tp;

    @JsonProperty("vat_status")
    private List<String> vatStatus;

    @JsonProperty("org_type")
    private String orgType;

    @JsonProperty("parent_account_id")
    private UUID parentAccountId;

    private List<Map<String, Object>> addresses;

    @JsonProperty("main_phone")
    private String mainPhone;

    @JsonProperty("corporate_email")
    private String corporateEmail;

    @JsonProperty("website_url")
    private String websiteUrl;

    private List<Map<String, Object>> personnel;

    @JsonProperty("created_by")
    private UUID createdBy;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static CustomerCardResponse fromEntity(CustomerCard c) {
        return CustomerCardResponse.builder()
                .id(c.getId())
                .partnerId(c.getPartnerId())
                .accountId(c.getAccountId())
                .recordType(c.getRecordType() != null ? c.getRecordType().name() : null)
                .name(c.getName())
                .searchName(c.getSearchName())
                .erpAccount(c.getErpAccount())
                .ice(c.getIce())
                .ifField(c.getIfNumber())
                .rc(c.getRc())
                .rcCity(c.getRcCity())
                .tp(c.getTp())
                .vatStatus(c.getVatStatus())
                .orgType(c.getOrgType() != null ? c.getOrgType().name() : null)
                .parentAccountId(c.getParentAccountId())
                .addresses(c.getAddresses())
                .mainPhone(c.getMainPhone())
                .corporateEmail(c.getCorporateEmail())
                .websiteUrl(c.getWebsiteUrl())
                .personnel(c.getPersonnel())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
