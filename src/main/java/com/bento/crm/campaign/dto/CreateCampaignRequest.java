package com.bento.crm.campaign.dto;

import com.bento.crm.campaign.model.Campaign;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    @NotBlank
    private String title;

    @NotNull
    private Campaign.Channel channel;

    @NotNull
    private Campaign.Status status;

    private UUID templateId;

    private UUID targetTagId;

    private Map<String, Object> targetFilter;

    private Instant scheduledAt;

    private Long sentCount;

    private Map<String, Object> metrics;
}
