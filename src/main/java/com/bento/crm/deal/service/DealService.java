package com.bento.crm.deal.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.deal.dto.CreateDealRequest;
import com.bento.crm.deal.model.Deal;
import com.bento.crm.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;

    @Transactional
    public Deal createDeal(CreateDealRequest request) {
        Deal deal = new Deal();
        applyRequest(deal, request);
        deal.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return dealRepository.save(deal);
    }

    public Deal getDeal(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return dealRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found"));
    }

    public Page<Deal> listDeals(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return dealRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public Deal updateDeal(UUID id, CreateDealRequest request) {
        Deal deal = getDeal(id);
        applyRequest(deal, request);
        return dealRepository.save(deal);
    }

    private void applyRequest(Deal deal, CreateDealRequest request) {
        deal.setPartnerId(request.getPartnerId());
        deal.setProposalId(request.getProposalId());
        deal.setTitle(request.getTitle());
        deal.setStage(request.getStage());
        deal.setAmount(request.getAmount());
        deal.setDiscount(request.getDiscount());
        deal.setComments(request.getComments());
        deal.setOrderNumber(request.getOrderNumber());
        deal.setOrderDate(request.getOrderDate());
        deal.setRequestedDeliveryDate(request.getRequestedDeliveryDate());
        deal.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        deal.setExpectedDeliveryDateVendor(request.getExpectedDeliveryDateVendor());
        deal.setDeliveryDate(request.getDeliveryDate());
        deal.setCustomerAccount(request.getCustomerAccount());
        deal.setBillingAddress(request.getBillingAddress());
        deal.setDeliveryAddress(request.getDeliveryAddress());
        deal.setContactPerson(request.getContactPerson());
        deal.setContactEmail(request.getContactEmail());
        deal.setContactPhone(request.getContactPhone());
        deal.setSalesPersonUserId(request.getSalesPersonUserId());
        deal.setSalesRegion(request.getSalesRegion());
        deal.setCurrency(request.getCurrency());
        deal.setPaymentTerms(request.getPaymentTerms());
        deal.setOrderTotalAmount(request.getOrderTotalAmount());
        deal.setVendorAccount(request.getVendorAccount());
        deal.setPurchaseOrderRef(request.getPurchaseOrderRef());
        deal.setWarehouseAddress(request.getWarehouseAddress());
        deal.setTransportationService(request.getTransportationService());
    }

    @Transactional
    public void deleteDeal(UUID id) {
        Deal deal = getDeal(id);
        dealRepository.delete(deal);
    }
}
