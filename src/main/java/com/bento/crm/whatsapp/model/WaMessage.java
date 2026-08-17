package com.bento.crm.whatsapp.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * Every message in or out, forming the conversation timeline the CRM renders.
 *
 * <p>{@code wamid} is Meta's own message id and carries a unique constraint: it is
 * the idempotency key that makes redelivered webhooks harmless.
 */
@Entity
@Table(name = "wa_message")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaMessage extends BaseTenantEntity {

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "campaign_id", columnDefinition = "uuid")
    private UUID campaignId;

    @Column(name = "recipient_id", columnDefinition = "uuid")
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Direction direction;

    @Column(unique = true)
    private String wamid;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(columnDefinition = "text")
    private String body;

    @Column(name = "template_name")
    private String templateName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_params", columnDefinition = "jsonb")
    private List<String> templateParams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_title", length = 500)
    private String errorTitle;

    /** 0 for the initial campaign send, 1 for the J+3 relance, and so on. */
    @Column(name = "sequence_step")
    private Integer sequenceStep;

    @Column(name = "sent_by_user_id", columnDefinition = "uuid")
    private UUID sentByUserId;

    public enum Direction {
        OUT, IN
    }

    public enum Status {
        QUEUED, SENT, DELIVERED, READ, FAILED, RECEIVED
    }
}
