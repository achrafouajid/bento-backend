package com.bento.crm.deal.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.deal.dto.CreateDealActivityRequest;
import com.bento.crm.deal.model.DealActivity;
import com.bento.crm.deal.repository.DealActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealActivityService {

    private final DealActivityRepository dealActivityRepository;
    private final DealService dealService;

    @Transactional
    public DealActivity createActivity(UUID dealId, CreateDealActivityRequest request) {
        dealService.getDeal(dealId);
        UUID orgId = TenantContext.getCurrentOrganizationId();

        DealActivity activity = new DealActivity();
        activity.setDealId(dealId);
        applyRequest(activity, request);
        if (activity.getOccurredAt() == null) {
            activity.setOccurredAt(Instant.now());
        }
        activity.setOrganizationId(orgId);
        return dealActivityRepository.save(activity);
    }

    public List<DealActivity> listActivities(UUID dealId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return dealActivityRepository.findByOrganizationIdAndDealId(orgId, dealId);
    }

    @Transactional
    public DealActivity updateActivity(UUID dealId, UUID activityId, CreateDealActivityRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        DealActivity activity = dealActivityRepository.findByOrganizationIdAndDealIdAndId(orgId, dealId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal activity not found"));
        Instant existingOccurredAt = activity.getOccurredAt();
        applyRequest(activity, request);
        if (activity.getOccurredAt() == null) {
            activity.setOccurredAt(existingOccurredAt);
        }
        return dealActivityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(UUID dealId, UUID activityId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        DealActivity activity = dealActivityRepository.findByOrganizationIdAndDealIdAndId(orgId, dealId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal activity not found"));
        dealActivityRepository.delete(activity);
    }

    private void applyRequest(DealActivity activity, CreateDealActivityRequest request) {
        activity.setType(DealActivity.Type.valueOf(request.getType().toUpperCase()));
        activity.setOccurredAt(request.getOccurredAt());
        activity.setDurationMinutes(request.getDurationMinutes());
        activity.setCallerName(request.getCallerName());
        activity.setOutcome(request.getOutcome());
        activity.setEmailFrom(request.getEmailFrom());
        activity.setEmailTo(request.getEmailTo());
        activity.setSubject(request.getSubject());
        activity.setBody(request.getBody());
        activity.setDirection(request.getDirection());
        activity.setTitle(request.getTitle());
        activity.setAttendees(request.getAttendees());
        activity.setLocation(request.getLocation());
        activity.setMeetingType(request.getMeetingType());
        activity.setMeetingLink(request.getMeetingLink());
        activity.setRecordingLink(request.getRecordingLink());
        activity.setDurationText(request.getDurationText());
        activity.setAuthor(request.getAuthor());
        activity.setContent(request.getContent());
        activity.setDueDate(request.getDueDate());
        activity.setAssignedTo(request.getAssignedTo());
        activity.setStatus(request.getStatus());
        activity.setSummary(request.getSummary());
    }
}
