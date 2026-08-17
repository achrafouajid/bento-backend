package com.bento.crm.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Live counters for a campaign, replacing the placeholder numbers on /marketing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatsResponse {

    private long total;
    private long pending;
    private long sent;
    private long delivered;
    private long read;
    private long replied;
    private long failed;
    private long skipped;
    private long optedOut;
    private long followupsPending;
    private long followupsSent;

    /** Replies as a percentage of messages that actually reached someone. */
    public double getReplyRate() {
        long reached = sent + delivered + read + replied;
        return reached == 0 ? 0.0 : Math.round((replied * 1000.0) / reached) / 10.0;
    }
}
