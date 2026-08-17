package com.bento.crm.whatsapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Polls for relances that have come due.
 *
 * <p>This replaces the "wait three days inside a workflow execution" approach. The
 * durable state lives in {@code wa_followup}, so relances survive restarts and
 * deploys, can be cancelled the moment a contact replies, and can be queried
 * ("what is going out this week?") like any other table.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WaFollowupScheduler {

    private final WaFollowupWorker worker;

    @Scheduled(
            initialDelayString = "${whatsapp.followup.initial-delay-ms:30000}",
            fixedDelayString = "${whatsapp.followup.poll-interval-ms:60000}")
    public void processDueFollowups() {
        List<UUID> claimed = worker.claimBatch();
        if (claimed.isEmpty()) {
            return;
        }

        log.info("[wa] claimed {} due relance(s)", claimed.size());
        for (UUID id : claimed) {
            try {
                worker.processOne(id);
            } catch (Exception e) {
                // One bad relance must not strand the rest of the claimed batch in
                // CLAIMED, where nothing would ever pick them up again.
                log.error("[wa] relance {} failed", id, e);
                worker.markFailed(id, e.getMessage());
            }
        }
    }
}
