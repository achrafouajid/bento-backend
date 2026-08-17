package com.bento.crm.deal.model;

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
import java.util.UUID;

@Entity
@Table(name = "deal_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealActivity extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID dealId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private Instant occurredAt;

    // Call
    private Integer durationMinutes;
    private String callerName;
    private String outcome;

    // Email
    private String emailFrom;
    private String emailTo;
    private String subject;
    @Column(columnDefinition = "text")
    private String body;
    private String direction;

    // Meeting
    private String title;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attendees;
    private String location;
    private String meetingType;

    // Recording
    private String meetingLink;
    private String recordingLink;
    private String durationText;

    // Note
    private String author;
    @Column(columnDefinition = "text")
    private String content;

    // Follow-up
    private Instant dueDate;
    private String assignedTo;
    private String status;

    @Column(columnDefinition = "text")
    private String summary;

    public enum Type {
        CALL, EMAIL, MEETING, RECORDING, NOTE, FOLLOW_UP
    }
}
