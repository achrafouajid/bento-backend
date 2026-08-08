package com.bento.crm.purchaseorder.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.purchaseorder.model.PurchaseOrder;
import com.bento.crm.purchaseorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase order management endpoints")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDERS_CREATE')")
    @Operation(summary = "Create purchase order", description = "Create a new purchase order")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrder po) {
        PurchaseOrder created = purchaseOrderService.createPurchaseOrder(po);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDERS_READ')")
    @Operation(summary = "Get purchase order by ID", description = "Retrieve purchase order details")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable UUID id) {
        PurchaseOrder po = purchaseOrderService.getPurchaseOrder(id);
        return ResponseEntity.ok(po);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDERS_READ')")
    @Operation(summary = "List purchase orders", description = "List all purchase orders in the organization")
    public ResponseEntity<PageResponse<PurchaseOrder>> listPurchaseOrders(Pageable pageable) {
        Page<PurchaseOrder> page = purchaseOrderService.listPurchaseOrders(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDERS_WRITE')")
    @Operation(summary = "Update purchase order", description = "Update purchase order information")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable UUID id, @Valid @RequestBody PurchaseOrder updates) {
        PurchaseOrder po = purchaseOrderService.updatePurchaseOrder(id, updates);
        return ResponseEntity.ok(po);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDERS_DELETE')")
    @Operation(summary = "Delete purchase order", description = "Delete purchase order record")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable UUID id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }
}
