package com.bento.crm.campaign.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One selected partner's state within a campaign. This is the row the /marketing
 * status table renders, and the row a relance is cancelled against when the
 * contact replies.
 */
@Entity
@Table(name = "campaign_recipient")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRecipient extends BaseTenantEntity {

    @Column(name = "campaign_id", nullable = false, columnDefinition = "uuid")
    private UUID campaignId;

    @Column(name = "partner_id", nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(name = "conversation_id", columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "phone_e164")
    private String phoneE164;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "replied_at")
    private Instant repliedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_title", length = 500)
    private String errorTitle;

    @Column(name = "followup_count", nullable = false)
    private Integer followupCount;

    @Column(name = "last_followup_at")
    private Instant lastFollowupAt;

    public enum Status {
        /** Selected but not yet dispatched. */
        PENDING,
        SENT,
        DELIVERED,
        READ,
        /** Terminal for relance purposes: the contact answered, so no follow-up is sent. */
        REPLIED,
        FAILED,
        /** Not attempted: no usable phone number. */
        SKIPPED,
        /** Not attempted: the contact previously sent STOP. */
        OPTED_OUT
    }

    /**
     * Delivery states never move backwards. Meta's status webhooks are not ordered,
     * so a {@code delivered} callback can arrive after {@code read}; without this
     * guard a contact who already replied could be downgraded to DELIVERED.
     */
    public boolean canAdvanceTo(Status next) {
        return rank(next) > rank(this.status);
    }

    private static int rank(Status s) {
        return switch (s) {
            case PENDING -> 0;
            case SKIPPED, OPTED_OUT -> 1;
            case FAILED -> 2;
            case SENT -> 3;
            case DELIVERED -> 4;
            case READ -> 5;
            case REPLIED -> 6;
        };
    }
}
