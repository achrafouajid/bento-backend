package com.bento.crm.ticket.dto;

import com.bento.crm.ticket.model.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private UUID partnerId;

    private UUID assignedToUserId;

    @NotNull
    private Ticket.Status status;

    private Ticket.Priority priority;

    private LocalDate deadline;

    private String resolution;
}
