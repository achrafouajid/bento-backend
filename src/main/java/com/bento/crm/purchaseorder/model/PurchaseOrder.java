package com.bento.crm.purchaseorder.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "purchase_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID dealId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID vendorPartnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private java.time.LocalDate deliveryDate;

    private String sentVia;

    public enum Status {
        DRAFT, SENT, CONFIRMED, DELIVERED, INVOICED
    }
}
