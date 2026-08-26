package com.bento.crm.identity.dto;

import com.bento.crm.identity.model.CrmGroup;
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
public class GroupResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private UUID createdBy;
    private UUID updatedBy;
    // Kept as Instant to match the entity: LocalDateTime.from(Instant) throws DateTimeException
    // at runtime because an Instant carries no local date or time fields.
    private Instant createdAt;
    private Instant updatedAt;

    public static GroupResponse fromEntity(CrmGroup crmGroup) {
        return GroupResponse.builder()
                .id(crmGroup.getId())
                .organizationId(crmGroup.getOrganizationId())
                .name(crmGroup.getName())
                .description(crmGroup.getDescription())
                .createdBy(crmGroup.getCreatedBy())
                .updatedBy(crmGroup.getUpdatedBy())
                .createdAt(crmGroup.getCreatedAt())
                .updatedAt(crmGroup.getUpdatedAt())
                .build();
    }
}
