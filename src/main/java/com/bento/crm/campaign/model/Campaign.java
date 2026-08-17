package com.bento.crm.campaign.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseTenantEntity {

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "uuid")
    private UUID templateId;

    @Column(columnDefinition = "uuid")
    private UUID targetTagId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> targetFilter;

    private Instant scheduledAt;

    private Long sentCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metrics;

    // --- WhatsApp channel ------------------------------------------------------
    // templateId above points at proposal_template; Meta identifies its own
    // approved templates by name, so those get dedicated columns.

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "template_lang")
    private String templateLang;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_params", columnDefinition = "jsonb")
    private List<String> templateParams;

    /** Rendered preview of the first send, for display in the CRM timeline. */
    @Column(name = "body_preview", columnDefinition = "text")
    private String bodyPreview;

    @Column(name = "followup_enabled", nullable = false)
    private Boolean followupEnabled;

    @Column(name = "followup_delay_days", nullable = false)
    private Integer followupDelayDays;

    @Column(name = "followup_template_name")
    private String followupTemplateName;

    /**
     * Test-only override. When set, relances are scheduled this many minutes out
     * instead of {@link #followupDelayDays}, turning a 3-day test cycle into a
     * 3-minute one.
     */
    @Column(name = "followup_delay_minutes")
    private Integer followupDelayMinutes;

    @Column(name = "launched_at")
    private Instant launchedAt;

    public enum Channel {
        WHATSAPP, SMS, EMAIL
    }

    public enum Status {
        DRAFT, SCHEDULED, SENDING, ACTIVE, COMPLETED
    }
}
