package com.bento.crm.invoice.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseTenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(columnDefinition = "uuid")
    private UUID dealId;

    @Column(columnDefinition = "uuid")
    private UUID purchaseOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDate dueDate;

    private Instant sentAt;

    private Instant paidAt;

    private String customerAccount;

    private String customerName;

    @Column(columnDefinition = "text")
    private String deliveryAddress;

    private String vatNumber;

    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal total;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> lines;

    public enum InvoiceType {
        CUSTOMER, VENDOR
    }

    public enum Status {
        DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE
    }
}
