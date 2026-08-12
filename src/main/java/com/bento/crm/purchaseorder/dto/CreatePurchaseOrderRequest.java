package com.bento.crm.purchaseorder.dto;

import com.bento.crm.purchaseorder.model.PurchaseOrder;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {

    @NotNull
    private UUID dealId;

    @NotNull
    private UUID vendorPartnerId;

    @NotNull
    private PurchaseOrder.Status status;

    private LocalDate deliveryDate;

    private String sentVia;
}
