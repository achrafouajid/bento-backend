package com.bento.crm.partner.service;

import com.bento.crm.partner.model.Partner;
import com.bento.crm.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Hard-deletes partners whose soft-delete grace period has run out.
 *
 * <p>Deliberately org-wide and outside any tenant request context: the job is not serving a
 * user, so there is no {@code TenantContext} to scope it by, and every organization's expired
 * records are due for the same treatment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PartnerPurgeScheduler {

    private final PartnerRepository partnerRepository;

    @Value("${partner.purge.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "${partner.purge.cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpired() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        List<Partner> expired = partnerRepository.findPurgeable(cutoff);
        if (expired.isEmpty()) {
            return;
        }
        partnerRepository.deleteAll(expired);
        log.info("[partner-purge] hard-deleted {} partner(s) soft-deleted before {}", expired.size(), cutoff);
    }
}
