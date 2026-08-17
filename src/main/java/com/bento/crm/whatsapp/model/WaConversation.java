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
 * One conversation per (organization, phone number), independent of how many
 * campaigns that number appears in.
 *
 * <p>{@code windowExpiresAt} tracks WhatsApp's 24-hour customer service window:
 * free-form text may only be sent while it is in the future. Outside it, only an
 * approved template will be accepted by Meta.
 */
@Entity
@Table(name = "wa_conversation")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaConversation extends BaseTenantEntity {

    @Column(name = "partner_id", columnDefinition = "uuid")
    private UUID partnerId;

    @Column(name = "phone_e164", nullable = false)
    private String phoneE164;

    @Column(name = "last_outbound_at")
    private Instant lastOutboundAt;

    @Column(name = "last_inbound_at")
    private Instant lastInboundAt;

    @Column(name = "window_expires_at")
    private Instant windowExpiresAt;

    @Column(name = "opted_out_at")
    private Instant optedOutAt;

    public boolean isWindowOpen() {
        return windowExpiresAt != null && windowExpiresAt.isAfter(Instant.now());
    }

    public boolean isOptedOut() {
        return optedOutAt != null;
    }
}
