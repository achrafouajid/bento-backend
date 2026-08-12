package com.bento.crm.campaign.dto;

import com.bento.crm.campaign.model.Campaign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {

    private UUID id;
    private UUID organizationId;
    private String title;
    private Campaign.Channel channel;
    private Campaign.Status status;
    private UUID templateId;
    private UUID targetTagId;
    private Map<String, Object> targetFilter;
    private Instant scheduledAt;
    private Long sentCount;
    private Map<String, Object> metrics;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CampaignResponse fromEntity(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .organizationId(campaign.getOrganizationId())
                .title(campaign.getTitle())
                .channel(campaign.getChannel())
                .status(campaign.getStatus())
                .templateId(campaign.getTemplateId())
                .targetTagId(campaign.getTargetTagId())
                .targetFilter(campaign.getTargetFilter())
                .scheduledAt(campaign.getScheduledAt())
                .sentCount(campaign.getSentCount())
                .metrics(campaign.getMetrics())
                .createdBy(campaign.getCreatedBy())
                .updatedBy(campaign.getUpdatedBy())
                .createdAt(campaign.getCreatedAt() != null ?
                    LocalDateTime.from(campaign.getCreatedAt()) : null)
                .updatedAt(campaign.getUpdatedAt() != null ?
                    LocalDateTime.from(campaign.getUpdatedAt()) : null)
                .build();
    }
}
