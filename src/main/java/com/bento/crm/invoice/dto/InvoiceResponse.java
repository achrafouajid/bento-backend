package com.bento.crm.invoice.dto;

import com.bento.crm.invoice.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private UUID id;
    private UUID organizationId;
    private Invoice.InvoiceType type;
    private UUID partnerId;
    private UUID dealId;
    private UUID purchaseOrderId;
    private Invoice.Status status;
    private LocalDate dueDate;
    private Instant sentAt;
    private Instant paidAt;
    private String customerAccount;
    private String customerName;
    private String deliveryAddress;
    private String vatNumber;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceResponse fromEntity(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .organizationId(invoice.getOrganizationId())
                .type(invoice.getType())
                .partnerId(invoice.getPartnerId())
                .dealId(invoice.getDealId())
                .purchaseOrderId(invoice.getPurchaseOrderId())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .sentAt(invoice.getSentAt())
                .paidAt(invoice.getPaidAt())
                .customerAccount(invoice.getCustomerAccount())
                .customerName(invoice.getCustomerName())
                .deliveryAddress(invoice.getDeliveryAddress())
                .vatNumber(invoice.getVatNumber())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .createdBy(invoice.getCreatedBy())
                .updatedBy(invoice.getUpdatedBy())
                .createdAt(invoice.getCreatedAt() != null ?
                    LocalDateTime.from(invoice.getCreatedAt()) : null)
                .updatedAt(invoice.getUpdatedAt() != null ?
                    LocalDateTime.from(invoice.getUpdatedAt()) : null)
                .build();
    }
}
