package com.bento.crm.ticket.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.common.model.RelatedEntityType;
import com.bento.crm.task.dto.TaskProgress;
import com.bento.crm.task.dto.TaskResponse;
import com.bento.crm.task.model.Task;
import com.bento.crm.ticket.dto.CreateTicketRequest;
import com.bento.crm.ticket.dto.CreateTicketTaskRequest;
import com.bento.crm.ticket.dto.TicketResponse;
import com.bento.crm.ticket.model.Ticket;
import com.bento.crm.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TICKETS_CREATE')")
    @Operation(summary = "Create ticket", description = "Create a new support ticket")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.fromEntity(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_READ')")
    @Operation(summary = "Get ticket by ID", description = "Retrieve ticket details")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket, ticketService.taskProgressFor(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TICKETS_READ')")
    @Operation(summary = "List tickets",
            description = "List tickets in the organization, optionally narrowed to those linked to a "
                    + "given record (e.g. every ticket opened against one customer or deal).")
    public ResponseEntity<PageResponse<TicketResponse>> listTickets(
            @Parameter(description = "Only tickets linked to this kind of record")
            @RequestParam(required = false) RelatedEntityType relatedEntityType,
            @Parameter(description = "Only tickets linked to this record id")
            @RequestParam(required = false) UUID relatedEntityId,
            Pageable pageable) {
        Page<Ticket> page = ticketService.listTickets(relatedEntityType, relatedEntityId, pageable);
        List<UUID> ids = page.getContent().stream().map(Ticket::getId).toList();
        Map<UUID, TaskProgress> progress = ticketService.taskProgressFor(ids);
        Page<TicketResponse> dtoPage = page.map(t ->
                TicketResponse.fromEntity(t, progress.getOrDefault(t.getId(), TaskProgress.none(t.getId()))));
        return ResponseEntity.ok(PageResponse.fromPage(dtoPage));
    }

    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasAuthority('TICKETS_READ') and hasAuthority('TASKS_READ')")
    @Operation(summary = "List a ticket's tasks",
            description = "The tasks raised for this ticket — the same rows as "
                    + "GET /tasks?relatedEntityType=TICKET&relatedEntityId={id}, but 404 on an unknown ticket.")
    public ResponseEntity<PageResponse<TaskResponse>> listTicketTasks(@PathVariable UUID id, Pageable pageable) {
        Page<TaskResponse> dtoPage = ticketService.listTasks(id, pageable).map(TaskResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.fromPage(dtoPage));
    }

    @PostMapping("/{id}/tasks")
    @PreAuthorize("hasAuthority('TICKETS_READ') and hasAuthority('TASKS_CREATE')")
    @Operation(summary = "Raise a task for a ticket",
            description = "Creates a task linked to this ticket. Assignee, priority and due date default to "
                    + "the ticket's own assignee, priority and deadline when omitted.")
    public ResponseEntity<TaskResponse> createTicketTask(@PathVariable UUID id,
                                                         @Valid @RequestBody CreateTicketTaskRequest request) {
        Task created = ticketService.createTask(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromEntity(created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_WRITE')")
    @Operation(summary = "Update ticket", description = "Update ticket information")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable UUID id, @Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_DELETE')")
    @Operation(summary = "Delete ticket", description = "Delete ticket record")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
