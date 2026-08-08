package com.bento.crm.deal.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
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
    public Deal createDeal(Deal deal) {
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
    public Deal updateDeal(UUID id, Deal updates) {
        Deal deal = getDeal(id);
        deal.setTitle(updates.getTitle());
        deal.setStage(updates.getStage());
        deal.setAmount(updates.getAmount());
        deal.setComments(updates.getComments());
        return dealRepository.save(deal);
    }

    @Transactional
    public void deleteDeal(UUID id) {
        Deal deal = getDeal(id);
        dealRepository.delete(deal);
    }
}
