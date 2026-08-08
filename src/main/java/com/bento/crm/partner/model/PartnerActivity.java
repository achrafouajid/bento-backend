package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partner_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerActivity extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    private String summary;

    @Column(columnDefinition = "text")
    private String detail;

    @Column(columnDefinition = "uuid")
    private UUID assignedToUserId;

    private Instant nextFollowUpAt;

    private Instant occurredAt;

    public enum ActivityType {
        CALL, EMAIL, MEETING, NOTE, TASK
    }
}
