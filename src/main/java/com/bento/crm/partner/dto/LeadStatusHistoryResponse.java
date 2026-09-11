package com.bento.crm.partner.dto;

import com.bento.crm.partner.model.LeadStatusHistory;
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
public class LeadStatusHistoryResponse {

    private UUID id;

    @JsonProperty("partner_id")
    private UUID partnerId;

    private String status;

    @JsonProperty("changed_at")
    private Instant changedAt;

    @JsonProperty("changed_by_user_id")
    private UUID changedByUserId;

    public static LeadStatusHistoryResponse fromEntity(LeadStatusHistory history) {
        return LeadStatusHistoryResponse.builder()
                .id(history.getId())
                .partnerId(history.getPartnerId())
                .status(history.getStatus())
                .changedAt(history.getChangedAt())
                .changedByUserId(history.getChangedByUserId())
                .build();
    }
}
