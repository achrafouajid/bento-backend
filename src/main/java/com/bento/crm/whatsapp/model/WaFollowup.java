package com.bento.crm.whatsapp.model;

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
 * A scheduled relance. Rows are claimed by the scheduler with
 * {@code FOR UPDATE SKIP LOCKED}, so several application instances can poll the
 * same table without ever sending the same relance twice.
 *
 * <p>A partial unique index in V13 keeps at most one PENDING row per
 * (campaign, conversation).
 */
@Entity
@Table(name = "wa_followup")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaFollowup extends BaseTenantEntity {

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "campaign_id", nullable = false, columnDefinition = "uuid")
    private UUID campaignId;

    @Column(name = "recipient_id", nullable = false, columnDefinition = "uuid")
    private UUID recipientId;

    @Column(name = "trigger_message_id", columnDefinition = "uuid")
    private UUID triggerMessageId;

    @Column(name = "sent_message_id", columnDefinition = "uuid")
    private UUID sentMessageId;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "sequence_step", nullable = false)
    private Integer sequenceStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    public enum State {
        PENDING, CANCELLED, CLAIMED, SENT, FAILED, SKIPPED
    }
}
