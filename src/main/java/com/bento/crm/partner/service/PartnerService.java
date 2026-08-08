package com.bento.crm.partner.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.partner.dto.CreatePartnerRequest;
import com.bento.crm.partner.model.Partner;
import com.bento.crm.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    @Transactional
    public Partner createPartner(CreatePartnerRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        Partner partner = Partner.builder()
                .type(Partner.PartnerType.valueOf(request.getType()))
                .name(request.getName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .country(request.getCountry())
                .source(request.getSource() != null ? Partner.PartnerSource.valueOf(request.getSource()) : null)
                .score(request.getScore())
                .temperature(request.getTemperature() != null ? Partner.Temperature.valueOf(request.getTemperature()) : null)
                .priority(request.getPriority() != null ? Partner.Priority.valueOf(request.getPriority()) : null)
                .stage(Partner.PartnerStage.valueOf(request.getStage() != null ? request.getStage() : "NEW"))
                .estimatedDealValue(request.getEstimatedDealValue())
                .probability(request.getProbability())
                .comments(request.getComments())
                .build();
        partner.setOrganizationId(orgId);

        return partnerRepository.save(partner);
    }

    public Partner getPartner(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return partnerRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
    }

    public Page<Partner> listPartners(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return partnerRepository.findByOrganizationId(orgId, pageable);
    }

    public Page<Partner> listPartnersByType(Partner.PartnerType type, Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return partnerRepository.findByOrganizationIdAndType(orgId, type, pageable);
    }

    public Page<Partner> listPartnersByStage(Partner.PartnerStage stage, Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return partnerRepository.findByOrganizationIdAndStage(orgId, stage, pageable);
    }

    @Transactional
    public Partner updatePartner(UUID id, CreatePartnerRequest request) {
        Partner partner = getPartner(id);

        partner.setName(request.getName());
        partner.setCompanyName(request.getCompanyName());
        partner.setEmail(request.getEmail());
        partner.setPhone(request.getPhone());
        partner.setCity(request.getCity());
        partner.setCountry(request.getCountry());
        partner.setScore(request.getScore());
        partner.setTemperature(request.getTemperature() != null ? Partner.Temperature.valueOf(request.getTemperature()) : null);
        partner.setPriority(request.getPriority() != null ? Partner.Priority.valueOf(request.getPriority()) : null);
        partner.setComments(request.getComments());

        return partnerRepository.save(partner);
    }

    @Transactional
    public void deletePartner(UUID id) {
        Partner partner = getPartner(id);
        partnerRepository.delete(partner);
    }
}
