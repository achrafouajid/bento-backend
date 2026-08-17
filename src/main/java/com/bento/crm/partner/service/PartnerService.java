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

import java.util.List;
import java.util.Map;
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
                .qualification(request.getQualification() != null ? Partner.Qualification.valueOf(request.getQualification()) : null)
                .stage(Partner.PartnerStage.valueOf(request.getStage() != null ? request.getStage() : "NEW"))
                .assignedToUserId(request.getAssignedToUserId() != null ? UUID.fromString(request.getAssignedToUserId()) : null)
                .ownerId(request.getOwnerId() != null ? UUID.fromString(request.getOwnerId()) : null)
                .estimatedDealValue(request.getEstimatedDealValue())
                .probability(request.getProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
                .comments(request.getComments())
                .company(request.getCompany())
                .productInterests(request.getProductInterests() != null ? request.getProductInterests() : List.<Map<String, Object>>of())
                .campaigns(request.getCampaigns() != null ? request.getCampaigns() : List.<Map<String, Object>>of())
                .notes(request.getNotes())
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

        partner.setType(Partner.PartnerType.valueOf(request.getType()));
        partner.setName(request.getName());
        partner.setCompanyName(request.getCompanyName());
        partner.setEmail(request.getEmail());
        partner.setPhone(request.getPhone());
        partner.setCity(request.getCity());
        partner.setCountry(request.getCountry());
        partner.setSource(request.getSource() != null ? Partner.PartnerSource.valueOf(request.getSource()) : null);
        partner.setScore(request.getScore());
        partner.setTemperature(request.getTemperature() != null ? Partner.Temperature.valueOf(request.getTemperature()) : null);
        partner.setPriority(request.getPriority() != null ? Partner.Priority.valueOf(request.getPriority()) : null);
        partner.setQualification(request.getQualification() != null ? Partner.Qualification.valueOf(request.getQualification()) : null);
        if (request.getStage() != null) {
            partner.setStage(Partner.PartnerStage.valueOf(request.getStage()));
        }
        partner.setAssignedToUserId(request.getAssignedToUserId() != null ? UUID.fromString(request.getAssignedToUserId()) : null);
        partner.setOwnerId(request.getOwnerId() != null ? UUID.fromString(request.getOwnerId()) : null);
        partner.setEstimatedDealValue(request.getEstimatedDealValue());
        partner.setProbability(request.getProbability());
        partner.setExpectedCloseDate(request.getExpectedCloseDate());
        partner.setComments(request.getComments());
        partner.setCompany(request.getCompany());
        partner.setProductInterests(request.getProductInterests() != null ? request.getProductInterests() : List.<Map<String, Object>>of());
        partner.setCampaigns(request.getCampaigns() != null ? request.getCampaigns() : List.<Map<String, Object>>of());
        partner.setNotes(request.getNotes());

        return partnerRepository.save(partner);
    }

    @Transactional
    public void deletePartner(UUID id) {
        Partner partner = getPartner(id);
        partnerRepository.delete(partner);
    }
}
