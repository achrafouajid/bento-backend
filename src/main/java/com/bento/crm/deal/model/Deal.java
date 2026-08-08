package com.bento.crm.deal.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "deal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(columnDefinition = "uuid")
    private UUID proposalId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStage stage;

    private BigDecimal amount;

    private BigDecimal discount;

    @Column(columnDefinition = "text")
    private String comments;

    private String orderNumber;

    private LocalDate orderDate;

    private LocalDate requestedDeliveryDate;

    private LocalDate estimatedDeliveryDate;

    private LocalDate expectedDeliveryDateVendor;

    private LocalDate deliveryDate;

    private String customerAccount;

    @Column(columnDefinition = "text")
    private String billingAddress;

    @Column(columnDefinition = "text")
    private String deliveryAddress;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    @Column(columnDefinition = "uuid")
    private UUID salesPersonUserId;

    private String salesRegion;

    private String currency;

    private String paymentTerms;

    private BigDecimal orderTotalAmount;

    private String vendorAccount;

    private String purchaseOrderRef;

    @Column(columnDefinition = "text")
    private String warehouseAddress;

    private String transportationService;

    public enum DealStage {
        OPEN, PO_SENT, AWAITING_DELIVERY, AWAITING_INVOICING,
        INVOICED, PAID, OVERDUE, CLOSED_WON, CLOSED_LOST
    }
}
