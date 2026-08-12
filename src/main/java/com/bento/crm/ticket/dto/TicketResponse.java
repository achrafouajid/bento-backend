package com.bento.crm.ticket.dto;

import com.bento.crm.ticket.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private UUID id;
    private UUID organizationId;
    private String title;
    private String description;
    private UUID partnerId;
    private UUID assignedToUserId;
    private Ticket.Status status;
    private Ticket.Priority priority;
    private LocalDate deadline;
    private String resolution;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .organizationId(ticket.getOrganizationId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .partnerId(ticket.getPartnerId())
                .assignedToUserId(ticket.getAssignedToUserId())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .deadline(ticket.getDeadline())
                .resolution(ticket.getResolution())
                .createdBy(ticket.getCreatedBy())
                .updatedBy(ticket.getUpdatedBy())
                .createdAt(ticket.getCreatedAt() != null ?
                    LocalDateTime.from(ticket.getCreatedAt()) : null)
                .updatedAt(ticket.getUpdatedAt() != null ?
                    LocalDateTime.from(ticket.getUpdatedAt()) : null)
                .build();
    }
}
