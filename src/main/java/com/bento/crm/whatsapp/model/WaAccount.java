package com.bento.crm.whatsapp.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A tenant's WhatsApp sending identity. One row per organization.
 *
 * <p>{@code phoneNumberId} is unique across all organizations because it is the only
 * key Meta's inbound webhook carries — it is how an unauthenticated callback is
 * resolved back to a tenant.
 */
@Entity
@Table(name = "wa_account")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaAccount extends BaseTenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "phone_number_id", nullable = false)
    private String phoneNumberId;

    @Column(name = "waba_id")
    private String wabaId;

    @Column(name = "display_phone_number")
    private String displayPhoneNumber;

    @Column(name = "access_token", columnDefinition = "text")
    private String accessToken;

    @Column(name = "app_secret")
    private String appSecret;

    @Column(name = "verify_token")
    private String verifyToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "quality_rating")
    private String qualityRating;

    public enum Provider {
        /** Simulated transport: no Meta account required, used to exercise the full flow. */
        MOCK,
        META
    }

    public enum Status {
        CONNECTED, DISCONNECTED
    }
}
