package com.bento.crm.deal.dto;

import com.bento.crm.deal.model.DealActivity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealActivityResponse {

    private UUID id;

    @JsonProperty("deal_id")
    private UUID dealId;

    private DealActivity.Type type;

    @JsonProperty("occurred_at")
    private Instant occurredAt;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("caller_name")
    private String callerName;

    private String outcome;

    @JsonProperty("email_from")
    private String emailFrom;

    @JsonProperty("email_to")
    private String emailTo;

    private String subject;

    private String body;

    private String direction;

    private String title;

    private List<String> attendees;

    private String location;

    @JsonProperty("meeting_type")
    private String meetingType;

    @JsonProperty("meeting_link")
    private String meetingLink;

    @JsonProperty("recording_link")
    private String recordingLink;

    @JsonProperty("duration_text")
    private String durationText;

    private String author;

    private String content;

    @JsonProperty("due_date")
    private Instant dueDate;

    @JsonProperty("assigned_to")
    private String assignedTo;

    private String status;

    private String summary;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static DealActivityResponse fromEntity(DealActivity activity) {
        return DealActivityResponse.builder()
                .id(activity.getId())
                .dealId(activity.getDealId())
                .type(activity.getType())
                .occurredAt(activity.getOccurredAt())
                .durationMinutes(activity.getDurationMinutes())
                .callerName(activity.getCallerName())
                .outcome(activity.getOutcome())
                .emailFrom(activity.getEmailFrom())
                .emailTo(activity.getEmailTo())
                .subject(activity.getSubject())
                .body(activity.getBody())
                .direction(activity.getDirection())
                .title(activity.getTitle())
                .attendees(activity.getAttendees())
                .location(activity.getLocation())
                .meetingType(activity.getMeetingType())
                .meetingLink(activity.getMeetingLink())
                .recordingLink(activity.getRecordingLink())
                .durationText(activity.getDurationText())
                .author(activity.getAuthor())
                .content(activity.getContent())
                .dueDate(activity.getDueDate())
                .assignedTo(activity.getAssignedTo())
                .status(activity.getStatus())
                .summary(activity.getSummary())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}
