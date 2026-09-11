package com.bento.crm.ticket.dto;

import com.bento.crm.task.model.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A task raised from inside a ticket. Only the title is required: the link back to the ticket is
 * implied by the URL, the creator is the caller, and assignee / priority / due date fall back to
 * the ticket's own values so a one-line "add sub-task" form produces a fully-formed task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketTaskRequest {

    @NotBlank
    private String title;

    private String description;

    private UUID assignedToUserId;

    private UUID assignedTeamId;

    private Task.TaskStatus status;

    private Task.Priority priority;

    private LocalDate dueDate;
}
