package com.bento.crm.partner.dto;

import com.bento.crm.partner.model.LeadContact;
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
public class LeadContactResponse {

    private UUID id;

    @JsonProperty("partner_id")
    private UUID partnerId;

    private String name;

    @JsonProperty("job_title")
    private String jobTitle;

    private String email;

    private String phone;

    private String mobile;

    private String website;

    private String linkedin;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static LeadContactResponse fromEntity(LeadContact contact) {
        return LeadContactResponse.builder()
                .id(contact.getId())
                .partnerId(contact.getPartnerId())
                .name(contact.getName())
                .jobTitle(contact.getJobTitle())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .mobile(contact.getMobile())
                .website(contact.getWebsite())
                .linkedin(contact.getLinkedin())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
