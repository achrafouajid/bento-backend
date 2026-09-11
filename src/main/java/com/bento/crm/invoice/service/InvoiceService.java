package com.bento.crm.invoice.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.common.service.BusinessNumberService;
import com.bento.crm.invoice.dto.CreateInvoiceRequest;
import com.bento.crm.invoice.model.Invoice;
import com.bento.crm.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BusinessNumberService businessNumberService;

    /** Moroccan standard VAT, applied when the client sends lines but no tax breakdown. */
    static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.20");

    @Transactional
    public Invoice createInvoice(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setOrganizationId(TenantContext.getCurrentOrganizationId());
        applyRequest(invoice, request);
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return invoiceRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public Page<Invoice> listInvoices(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return invoiceRepository.findByOrganizationId(orgId, pageable);
    }

    public Page<Invoice> listInvoicesByStatus(Invoice.Status status, Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return invoiceRepository.findByOrganizationIdAndStatus(orgId, status, pageable);
    }

    @Transactional
    public Invoice updateInvoice(UUID id, CreateInvoiceRequest request) {
        Invoice invoice = getInvoice(id);
        applyRequest(invoice, request);
        return invoiceRepository.save(invoice);
    }

    private void applyRequest(Invoice invoice, CreateInvoiceRequest request) {
        applyRequestFields(invoice, request);
        fillDerivedFields(invoice);
    }

    /**
     * What the API does not receive it derives, so an invoice created from a one-line form is
     * still complete: a sequential number, an issue date, the sent/paid timestamps implied by
     * the status, and a subtotal/VAT/total breakdown computed from the lines.
     */
    private void fillDerivedFields(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            String prefix = invoice.getType() == Invoice.InvoiceType.VENDOR ? "FRS" : "FAC";
            invoice.setInvoiceNumber(businessNumberService.next(invoice.getOrganizationId(), prefix));
        }
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        boolean sent = invoice.getStatus() != null && invoice.getStatus() != Invoice.Status.DRAFT;
        if (sent && invoice.getSentAt() == null) {
            invoice.setSentAt(Instant.now());
        }
        if (invoice.getStatus() == Invoice.Status.PAID && invoice.getPaidAt() == null) {
            invoice.setPaidAt(Instant.now());
        }

        BigDecimal fromLines = sumOfLines(invoice.getLines());
        if (invoice.getSubtotal() == null) {
            // Prefer the lines; otherwise treat a lone total as the tax-exclusive amount the
            // form labels "Subtotal".
            invoice.setSubtotal(fromLines != null ? fromLines : invoice.getTotal());
        }
        if (invoice.getSubtotal() != null) {
            if (invoice.getTax() == null) {
                invoice.setTax(invoice.getSubtotal().multiply(DEFAULT_VAT_RATE).setScale(2, RoundingMode.HALF_UP));
            }
            if (invoice.getTotal() == null || fromLines != null || invoice.getTotal().compareTo(invoice.getSubtotal()) == 0) {
                invoice.setTotal(invoice.getSubtotal().add(invoice.getTax()));
            }
        }
    }

    /** Σ qty × unitPrice over the JSON lines, or {@code null} when there are no usable lines. */
    static BigDecimal sumOfLines(List<Map<String, Object>> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        boolean any = false;
        for (Map<String, Object> line : lines) {
            BigDecimal qty = toDecimal(line.get("qty"));
            BigDecimal unitPrice = toDecimal(line.get("unitPrice"));
            if (unitPrice == null) {
                unitPrice = toDecimal(line.get("unit_price"));
            }
            if (qty == null || unitPrice == null) {
                continue;
            }
            sum = sum.add(qty.multiply(unitPrice));
            any = true;
        }
        return any ? sum.setScale(2, RoundingMode.HALF_UP) : null;
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal d) {
            return d;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyRequestFields(Invoice invoice, CreateInvoiceRequest request) {
        invoice.setType(request.getType());
        invoice.setPartnerId(request.getPartnerId());
        invoice.setDealId(request.getDealId());
        invoice.setPurchaseOrderId(request.getPurchaseOrderId());
        invoice.setStatus(request.getStatus());
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setSentAt(request.getSentAt());
        invoice.setPaidAt(request.getPaidAt());
        invoice.setCustomerAccount(request.getCustomerAccount());
        invoice.setCustomerName(request.getCustomerName());
        invoice.setDeliveryAddress(request.getDeliveryAddress());
        invoice.setVatNumber(request.getVatNumber());
        invoice.setSubtotal(request.getSubtotal());
        invoice.setTax(request.getTax());
        invoice.setTotal(request.getTotal());
        invoice.setLines(request.getLines() != null ? request.getLines() : new java.util.ArrayList<>());
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = getInvoice(id);
        invoiceRepository.delete(invoice);
    }
}
