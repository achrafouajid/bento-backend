package com.bento.crm.invoice.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNote extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID invoiceId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    private String reason;

    private BigDecimal amount;

    private Instant issuedAt;
}
