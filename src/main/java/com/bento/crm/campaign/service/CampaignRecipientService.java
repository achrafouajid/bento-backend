package com.bento.crm.campaign.service;

import com.bento.crm.campaign.dto.CampaignRecipientResponse;
import com.bento.crm.campaign.dto.CampaignStatsResponse;
import com.bento.crm.campaign.model.CampaignRecipient;
import com.bento.crm.campaign.repository.CampaignRecipientRepository;
import com.bento.crm.partner.model.Partner;
import com.bento.crm.partner.repository.PartnerRepository;
import com.bento.crm.whatsapp.model.WaFollowup;
import com.bento.crm.whatsapp.repository.WaFollowupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read side of campaign progress: the per-recipient table and headline counters
 * shown on /marketing.
 */
@Service
@RequiredArgsConstructor
public class CampaignRecipientService {

    private final CampaignRecipientRepository recipientRepository;
    private final WaFollowupRepository followupRepository;
    private final PartnerRepository partnerRepository;

    @Transactional(readOnly = true)
    public List<CampaignRecipientResponse> listRecipients(UUID orgId, UUID campaignId) {
        List<CampaignRecipient> recipients = recipientRepository.findAllByCampaign(orgId, campaignId);

        // Two bulk lookups rather than a query per row: a 500-contact campaign would
        // otherwise issue 1000 queries to render one table.
        Set<UUID> partnerIds = recipients.stream()
                .map(CampaignRecipient::getPartnerId)
                .collect(Collectors.toSet());

        Map<UUID, String> names = partnerIds.isEmpty()
                ? Map.of()
                : partnerRepository.findAllById(partnerIds).stream()
                        .collect(Collectors.toMap(Partner::getId, Partner::getName, (a, b) -> a));

        Map<UUID, Instant> dueDates = new HashMap<>();
        for (Object[] row : followupRepository.findDueDatesForCampaign(orgId, campaignId, WaFollowup.State.PENDING)) {
            dueDates.put((UUID) row[0], (Instant) row[1]);
        }

        return recipients.stream()
                .map(r -> CampaignRecipientResponse.fromEntity(
                        r,
                        names.getOrDefault(r.getPartnerId(), "Unknown contact"),
                        dueDates.get(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CampaignStatsResponse stats(UUID orgId, UUID campaignId) {
        Map<CampaignRecipient.Status, Long> counts = new EnumMap<>(CampaignRecipient.Status.class);
        for (Object[] row : recipientRepository.countByStatusForCampaign(orgId, campaignId)) {
            counts.put((CampaignRecipient.Status) row[0], (Long) row[1]);
        }

        return CampaignStatsResponse.builder()
                .total(counts.values().stream().mapToLong(Long::longValue).sum())
                .pending(counts.getOrDefault(CampaignRecipient.Status.PENDING, 0L))
                .sent(counts.getOrDefault(CampaignRecipient.Status.SENT, 0L))
                .delivered(counts.getOrDefault(CampaignRecipient.Status.DELIVERED, 0L))
                .read(counts.getOrDefault(CampaignRecipient.Status.READ, 0L))
                .replied(counts.getOrDefault(CampaignRecipient.Status.REPLIED, 0L))
                .failed(counts.getOrDefault(CampaignRecipient.Status.FAILED, 0L))
                .skipped(counts.getOrDefault(CampaignRecipient.Status.SKIPPED, 0L))
                .optedOut(counts.getOrDefault(CampaignRecipient.Status.OPTED_OUT, 0L))
                .followupsPending(followupRepository.countForCampaignInState(
                        orgId, campaignId, WaFollowup.State.PENDING))
                .followupsSent(followupRepository.countForCampaignInState(
                        orgId, campaignId, WaFollowup.State.SENT))
                .build();
    }
}
