package com.bento.crm.campaign.dto;

import com.bento.crm.campaign.model.CampaignRecipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRecipientResponse {

    private UUID id;
    private UUID campaignId;
    private UUID partnerId;
    private String partnerName;
    private UUID conversationId;
    private String phone;
    private String status;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;
    private Instant repliedAt;
    private Instant failedAt;
    private String errorCode;
    private String errorTitle;
    private Integer followupCount;
    private Instant lastFollowupAt;
    /** When the queued relance is due, or null when none is pending. */
    private Instant followupDueAt;

    public static CampaignRecipientResponse fromEntity(CampaignRecipient r, String partnerName, Instant followupDueAt) {
        return CampaignRecipientResponse.builder()
                .id(r.getId())
                .campaignId(r.getCampaignId())
                .partnerId(r.getPartnerId())
                .partnerName(partnerName)
                .conversationId(r.getConversationId())
                .phone(r.getPhoneE164())
                .status(r.getStatus().name())
                .sentAt(r.getSentAt())
                .deliveredAt(r.getDeliveredAt())
                .readAt(r.getReadAt())
                .repliedAt(r.getRepliedAt())
                .failedAt(r.getFailedAt())
                .errorCode(r.getErrorCode())
                .errorTitle(r.getErrorTitle())
                .followupCount(r.getFollowupCount())
                .lastFollowupAt(r.getLastFollowupAt())
                .followupDueAt(followupDueAt)
                .build();
    }
}
