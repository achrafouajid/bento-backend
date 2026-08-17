package com.bento.crm.partner.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.partner.dto.CustomerCardRequest;
import com.bento.crm.partner.model.CustomerCard;
import com.bento.crm.partner.repository.CustomerCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerCardService {

    private final PartnerService partnerService;
    private final CustomerCardRepository customerCardRepository;

    public CustomerCard getCard(UUID partnerId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return customerCardRepository.findByOrganizationIdAndPartnerId(orgId, partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer card not found"));
    }

    @Transactional
    public CustomerCard saveCard(UUID partnerId, CustomerCardRequest request) {
        partnerService.getPartner(partnerId);
        UUID orgId = TenantContext.getCurrentOrganizationId();

        CustomerCard card = customerCardRepository.findByOrganizationIdAndPartnerId(orgId, partnerId)
                .orElseGet(() -> {
                    CustomerCard fresh = new CustomerCard();
                    fresh.setOrganizationId(orgId);
                    fresh.setPartnerId(partnerId);
                    return fresh;
                });

        card.setAccountId(request.getAccountId());
        card.setRecordType(request.getRecordType() != null ? CustomerCard.RecordType.valueOf(request.getRecordType().toUpperCase()) : null);
        card.setName(request.getName());
        card.setSearchName(request.getSearchName());
        card.setErpAccount(request.getErpAccount());
        card.setIce(request.getIce());
        card.setIfNumber(request.getIfField());
        card.setRc(request.getRc());
        card.setRcCity(request.getRcCity());
        card.setTp(request.getTp());
        card.setVatStatus(request.getVatStatus());
        card.setOrgType(request.getOrgType() != null ? CustomerCard.OrgType.valueOf(request.getOrgType().toUpperCase()) : null);
        card.setParentAccountId(request.getParentAccountId() != null && !request.getParentAccountId().isBlank()
                ? UUID.fromString(request.getParentAccountId()) : null);
        card.setAddresses(request.getAddresses());
        card.setMainPhone(request.getMainPhone());
        card.setCorporateEmail(request.getCorporateEmail());
        card.setWebsiteUrl(request.getWebsiteUrl());
        card.setPersonnel(request.getPersonnel());

        return customerCardRepository.save(card);
    }
}
