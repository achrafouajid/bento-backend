package com.bento.crm.deal.dto;

import com.bento.crm.deal.model.Deal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealRequest {

    @NotNull
    private UUID partnerId;

    private UUID proposalId;

    @NotBlank
    private String title;

    @NotNull
    private Deal.DealStage stage;

    private BigDecimal amount;

    private BigDecimal discount;

    private String comments;

    private String orderNumber;

    private LocalDate orderDate;

    private LocalDate requestedDeliveryDate;

    private LocalDate estimatedDeliveryDate;

    private LocalDate expectedDeliveryDateVendor;

    private LocalDate deliveryDate;

    private String customerAccount;

    private String billingAddress;

    private String deliveryAddress;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    private UUID salesPersonUserId;

    private String salesRegion;

    private String currency;

    private String paymentTerms;

    private BigDecimal orderTotalAmount;

    private String vendorAccount;

    private String purchaseOrderRef;

    private String warehouseAddress;

    private String transportationService;
}
