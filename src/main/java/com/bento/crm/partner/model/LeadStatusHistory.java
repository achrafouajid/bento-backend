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
@Table(name = "lead_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStatusHistory extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(columnDefinition = "uuid")
    private UUID changedByUserId;
}
