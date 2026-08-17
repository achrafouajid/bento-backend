package com.bento.crm.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadActivityRequest {

    @NotBlank
    private String type;

    @JsonProperty("occurred_at")
    private Instant occurredAt;

    @NotBlank
    private String summary;

    private String detail;

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("next_follow_up_at")
    private Instant nextFollowUpAt;
}
