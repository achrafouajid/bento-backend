package com.bento.crm.invoice.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.invoice.dto.CreateInvoiceRequest;
import com.bento.crm.invoice.dto.InvoiceResponse;
import com.bento.crm.invoice.model.Invoice;
import com.bento.crm.invoice.service.InvoiceService;
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
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICES_CREATE')")
    @Operation(summary = "Create invoice", description = "Create a new invoice")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        Invoice created = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(InvoiceResponse.fromEntity(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES_READ')")
    @Operation(summary = "Get invoice by ID", description = "Retrieve invoice details")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        Invoice invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(InvoiceResponse.fromEntity(invoice));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICES_READ')")
    @Operation(summary = "List invoices", description = "List all invoices in the organization")
    public ResponseEntity<PageResponse<InvoiceResponse>> listInvoices(Pageable pageable) {
        Page<Invoice> page = invoiceService.listInvoices(pageable);
        Page<InvoiceResponse> dtoPage = page.map(InvoiceResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.fromPage(dtoPage));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES_WRITE')")
    @Operation(summary = "Update invoice", description = "Update invoice information")
    public ResponseEntity<InvoiceResponse> updateInvoice(@PathVariable UUID id, @Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(InvoiceResponse.fromEntity(invoice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES_DELETE')")
    @Operation(summary = "Delete invoice", description = "Delete invoice record")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
