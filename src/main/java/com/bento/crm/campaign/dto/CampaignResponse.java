package com.bento.crm.campaign.dto;

import com.bento.crm.campaign.model.Campaign;
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

    // WhatsApp channel
    private String templateName;
    private String templateLang;
    private List<String> templateParams;
    private String bodyPreview;
    private Boolean followupEnabled;
    private Integer followupDelayDays;
    private String followupTemplateName;
    private Integer followupDelayMinutes;
    private Instant launchedAt;

    private UUID createdBy;
    private UUID updatedBy;
    // These were previously converted with LocalDateTime.from(Instant), which throws
    // DateTimeException at runtime because an Instant carries no local date or time
    // fields. Keeping them as Instant matches the entity and the other DTOs.
    private Instant createdAt;
    private Instant updatedAt;

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
                .templateName(campaign.getTemplateName())
                .templateLang(campaign.getTemplateLang())
                .templateParams(campaign.getTemplateParams())
                .bodyPreview(campaign.getBodyPreview())
                .followupEnabled(campaign.getFollowupEnabled())
                .followupDelayDays(campaign.getFollowupDelayDays())
                .followupTemplateName(campaign.getFollowupTemplateName())
                .followupDelayMinutes(campaign.getFollowupDelayMinutes())
                .launchedAt(campaign.getLaunchedAt())
                .createdBy(campaign.getCreatedBy())
                .updatedBy(campaign.getUpdatedBy())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
