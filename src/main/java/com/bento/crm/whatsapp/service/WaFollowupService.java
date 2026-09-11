package com.bento.crm.whatsapp.service;

import com.bento.crm.campaign.model.Campaign;
import com.bento.crm.campaign.repository.CampaignRepository;
import com.bento.crm.whatsapp.model.WaFollowup;
import com.bento.crm.whatsapp.repository.WaFollowupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Scheduling and cancellation of relances.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WaFollowupService {

    private final WaFollowupRepository followupRepository;
    private final CampaignRepository campaignRepository;

    /**
     * Schedules the relance for a recipient, or pushes an existing one out.
     *
     * <p>The partial unique index allows only one PENDING relance per
     * (campaign, conversation). Rather than surfacing that as an error, a second
     * send to the same contact within a campaign moves the existing due date
     * forward — the agent messaging someone twice on day 2 should reset their
     * countdown, not queue a second relance.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleOrReschedule(UUID orgId,
                                     Campaign campaign,
                                     UUID conversationId,
                                     UUID recipientId,
                                     UUID triggerMessageId,
                                     int sequenceStep) {

        Instant dueAt = Instant.now().plus(delayFor(campaign));

        var existing = followupRepository.findPendingForCampaignConversation(
                orgId, campaign.getId(), conversationId, WaFollowup.State.PENDING);

        if (existing.isPresent()) {
            WaFollowup followup = existing.get();
            followup.setDueAt(dueAt);
            followup.setRecipientId(recipientId);
            followup.setTriggerMessageId(triggerMessageId);
            followup.setSequenceStep(sequenceStep);
            followupRepository.save(followup);
            return;
        }

        WaFollowup followup = new WaFollowup();
        followup.setOrganizationId(orgId);
        followup.setCampaignId(campaign.getId());
        followup.setConversationId(conversationId);
        followup.setRecipientId(recipientId);
        followup.setTriggerMessageId(triggerMessageId);
        followup.setDueAt(dueAt);
        followup.setSequenceStep(sequenceStep);
        followup.setState(WaFollowup.State.PENDING);
        followup.setAttempts(0);

        try {
            followupRepository.saveAndFlush(followup);
        } catch (DataIntegrityViolationException e) {
            // Lost the race against a concurrent send to the same contact; the row
            // that won is equivalent, so leave it in place.
            log.debug("[wa] relance already pending for campaign={} conversation={}",
                    campaign.getId(), conversationId);
        }
    }

    /**
     * Relance delay. {@code followupDelayMinutes} exists so the whole cycle can be
     * tested in minutes rather than waiting three days for each iteration.
     */
    private Duration delayFor(Campaign campaign) {
        if (campaign.getFollowupDelayMinutes() != null && campaign.getFollowupDelayMinutes() > 0) {
            return Duration.ofMinutes(campaign.getFollowupDelayMinutes());
        }
        int days = campaign.getFollowupDelayDays() == null ? 3 : campaign.getFollowupDelayDays();
        return Duration.ofDays(days);
    }

    /** Called when a contact replies: nothing pending for them should still fire. */
    @Transactional
    public int cancelForConversation(UUID conversationId) {
        return followupRepository.cancelPendingForConversation(
                conversationId, WaFollowup.State.PENDING, WaFollowup.State.CANCELLED, Instant.now());
    }

    @Transactional
    public int cancelForCampaign(UUID orgId, UUID campaignId) {
        int cancelled = followupRepository.cancelPendingForCampaign(
                orgId, campaignId, WaFollowup.State.PENDING, WaFollowup.State.CANCELLED, Instant.now());
        completeCampaignIfDone(orgId, campaignId);
        return cancelled;
    }

    /**
     * Moves a campaign to COMPLETED once its last relance resolves.
     *
     * <p>Dispatch leaves a campaign with relances enabled in ACTIVE, because work is
     * still outstanding. Without this it would stay ACTIVE forever: the sequence
     * would finish and the CRM would go on reporting the campaign as running.
     */
    @Transactional
    public void completeCampaignIfDone(UUID orgId, UUID campaignId) {
        long stillPending = followupRepository.countForCampaignInState(
                orgId, campaignId, WaFollowup.State.PENDING);
        if (stillPending > 0) {
            return;
        }
        campaignRepository.findByOrganizationIdAndId(orgId, campaignId).ifPresent(campaign -> {
            if (campaign.getStatus() == Campaign.Status.ACTIVE) {
                campaign.setStatus(Campaign.Status.COMPLETED);
                campaignRepository.save(campaign);
                log.info("[wa] campaign {} completed: no relances left pending", campaignId);
            }
        });
    }
}
