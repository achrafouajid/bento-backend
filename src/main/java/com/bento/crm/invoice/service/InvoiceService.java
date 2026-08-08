package com.bento.crm.invoice.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
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
    public Invoice createInvoice(Invoice invoice) {
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
    public Invoice updateInvoice(UUID id, Invoice updates) {
        Invoice invoice = getInvoice(id);
        invoice.setStatus(updates.getStatus());
        invoice.setPaidAt(updates.getPaidAt());
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = getInvoice(id);
        invoiceRepository.delete(invoice);
    }
}
