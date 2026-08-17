package com.bento.crm.partner.dto;

import com.bento.crm.partner.model.LeadActivity;
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
public class LeadActivityResponse {

    private UUID id;

    @JsonProperty("partner_id")
    private UUID partnerId;

    private LeadActivity.Type type;

    @JsonProperty("occurred_at")
    private Instant occurredAt;

    private String summary;

    private String detail;

    @JsonProperty("assigned_to_user_id")
    private UUID assignedToUserId;

    @JsonProperty("next_follow_up_at")
    private Instant nextFollowUpAt;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static LeadActivityResponse fromEntity(LeadActivity activity) {
        return LeadActivityResponse.builder()
                .id(activity.getId())
                .partnerId(activity.getPartnerId())
                .type(activity.getType())
                .occurredAt(activity.getOccurredAt())
                .summary(activity.getSummary())
                .detail(activity.getDetail())
                .assignedToUserId(activity.getAssignedToUserId())
                .nextFollowUpAt(activity.getNextFollowUpAt())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}
