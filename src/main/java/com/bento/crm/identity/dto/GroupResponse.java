package com.bento.crm.identity.dto;

import com.bento.crm.identity.model.CrmGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GroupResponse fromEntity(CrmGroup crmGroup) {
        return GroupResponse.builder()
                .id(crmGroup.getId())
                .organizationId(crmGroup.getOrganizationId())
                .name(crmGroup.getName())
                .description(crmGroup.getDescription())
                .createdBy(crmGroup.getCreatedBy())
                .updatedBy(crmGroup.getUpdatedBy())
                .createdAt(crmGroup.getCreatedAt() != null ?
                    LocalDateTime.from(crmGroup.getCreatedAt()) : null)
                .updatedAt(crmGroup.getUpdatedAt() != null ?
                    LocalDateTime.from(crmGroup.getUpdatedAt()) : null)
                .build();
    }
}
