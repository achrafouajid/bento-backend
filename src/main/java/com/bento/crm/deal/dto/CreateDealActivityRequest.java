package com.bento.crm.deal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealActivityRequest {

    @NotBlank
    private String type;

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
}
