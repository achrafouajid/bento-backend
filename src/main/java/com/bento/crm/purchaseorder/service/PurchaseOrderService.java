package com.bento.crm.purchaseorder.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.purchaseorder.model.PurchaseOrder;
import com.bento.crm.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrder po) {
        po.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder getPurchaseOrder(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return purchaseOrderRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));
    }

    public Page<PurchaseOrder> listPurchaseOrders(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return purchaseOrderRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrder(UUID id, PurchaseOrder updates) {
        PurchaseOrder po = getPurchaseOrder(id);
        po.setStatus(updates.getStatus());
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public void deletePurchaseOrder(UUID id) {
        PurchaseOrder po = getPurchaseOrder(id);
        purchaseOrderRepository.delete(po);
    }
}
