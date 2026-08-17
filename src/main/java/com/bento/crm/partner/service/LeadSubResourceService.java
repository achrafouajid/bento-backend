package com.bento.crm.partner.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.partner.dto.CreateLeadActivityRequest;
import com.bento.crm.partner.dto.CreateLeadContactRequest;
import com.bento.crm.partner.dto.CreateLeadStatusHistoryRequest;
import com.bento.crm.partner.model.LeadActivity;
import com.bento.crm.partner.model.LeadContact;
import com.bento.crm.partner.model.LeadStatusHistory;
import com.bento.crm.partner.repository.LeadActivityRepository;
import com.bento.crm.partner.repository.LeadContactRepository;
import com.bento.crm.partner.repository.LeadStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadSubResourceService {

    private final PartnerService partnerService;
    private final LeadContactRepository leadContactRepository;
    private final LeadActivityRepository leadActivityRepository;
    private final LeadStatusHistoryRepository leadStatusHistoryRepository;

    // Contacts

    @Transactional
    public LeadContact addContact(UUID partnerId, CreateLeadContactRequest request) {
        partnerService.getPartner(partnerId);
        UUID orgId = TenantContext.getCurrentOrganizationId();

        LeadContact contact = LeadContact.builder()
                .partnerId(partnerId)
                .name(request.getName())
                .jobTitle(request.getJobTitle())
                .email(request.getEmail())
                .phone(request.getPhone())
                .mobile(request.getMobile())
                .website(request.getWebsite())
                .linkedin(request.getLinkedin())
                .build();
        contact.setOrganizationId(orgId);
        return leadContactRepository.save(contact);
    }

    public List<LeadContact> listContacts(UUID partnerId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return leadContactRepository.findByOrganizationIdAndPartnerId(orgId, partnerId);
    }

    @Transactional
    public LeadContact updateContact(UUID partnerId, UUID contactId, CreateLeadContactRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        LeadContact contact = leadContactRepository.findByOrganizationIdAndPartnerIdAndId(orgId, partnerId, contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead contact not found"));

        contact.setName(request.getName());
        contact.setJobTitle(request.getJobTitle());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setMobile(request.getMobile());
        contact.setWebsite(request.getWebsite());
        contact.setLinkedin(request.getLinkedin());
        return leadContactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(UUID partnerId, UUID contactId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        LeadContact contact = leadContactRepository.findByOrganizationIdAndPartnerIdAndId(orgId, partnerId, contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead contact not found"));
        leadContactRepository.delete(contact);
    }

    // Activities

    @Transactional
    public LeadActivity addActivity(UUID partnerId, CreateLeadActivityRequest request) {
        partnerService.getPartner(partnerId);
        UUID orgId = TenantContext.getCurrentOrganizationId();

        LeadActivity activity = LeadActivity.builder()
                .partnerId(partnerId)
                .type(LeadActivity.Type.valueOf(request.getType().toUpperCase()))
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : Instant.now())
                .summary(request.getSummary())
                .detail(request.getDetail())
                .assignedToUserId(request.getAssignedToUserId() != null ? UUID.fromString(request.getAssignedToUserId()) : null)
                .nextFollowUpAt(request.getNextFollowUpAt())
                .build();
        activity.setOrganizationId(orgId);
        return leadActivityRepository.save(activity);
    }

    public List<LeadActivity> listActivities(UUID partnerId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return leadActivityRepository.findByOrganizationIdAndPartnerId(orgId, partnerId);
    }

    @Transactional
    public void deleteActivity(UUID partnerId, UUID activityId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        LeadActivity activity = leadActivityRepository.findByOrganizationIdAndPartnerIdAndId(orgId, partnerId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead activity not found"));
        leadActivityRepository.delete(activity);
    }

    // Status history

    @Transactional
    public LeadStatusHistory addStatusHistory(UUID partnerId, CreateLeadStatusHistoryRequest request) {
        partnerService.getPartner(partnerId);
        UUID orgId = TenantContext.getCurrentOrganizationId();

        LeadStatusHistory history = LeadStatusHistory.builder()
                .partnerId(partnerId)
                .status(request.getStatus())
                .changedAt(Instant.now())
                .changedByUserId(request.getChangedByUserId() != null ? UUID.fromString(request.getChangedByUserId()) : null)
                .build();
        history.setOrganizationId(orgId);
        return leadStatusHistoryRepository.save(history);
    }

    public List<LeadStatusHistory> listStatusHistory(UUID partnerId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return leadStatusHistoryRepository.findByOrganizationIdAndPartnerId(orgId, partnerId);
    }
}
