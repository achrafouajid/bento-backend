package com.bento.crm.invoice.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.invoice.dto.CreateInvoiceRequest;
import com.bento.crm.invoice.model.Invoice;
import com.bento.crm.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        applyRequest(invoice, request);
        invoice.setOrganizationId(TenantContext.getCurrentOrganizationId());
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
        invoice.setType(request.getType());
        invoice.setPartnerId(request.getPartnerId());
        invoice.setDealId(request.getDealId());
        invoice.setPurchaseOrderId(request.getPurchaseOrderId());
        invoice.setStatus(request.getStatus());
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
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = getInvoice(id);
        invoiceRepository.delete(invoice);
    }
}
