package com.bento.crm.ticket.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.ticket.model.Ticket;
import com.bento.crm.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
        Ticket created = ticketService.createTicket(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_READ')")
    @Operation(summary = "Get ticket by ID", description = "Retrieve ticket details")
    public ResponseEntity<Ticket> getTicket(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TICKETS_READ')")
    @Operation(summary = "List tickets", description = "List all tickets in the organization")
    public ResponseEntity<PageResponse<Ticket>> listTickets(Pageable pageable) {
        Page<Ticket> page = ticketService.listTickets(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_WRITE')")
    @Operation(summary = "Update ticket", description = "Update ticket information")
    public ResponseEntity<Ticket> updateTicket(@PathVariable UUID id, @Valid @RequestBody Ticket updates) {
        Ticket ticket = ticketService.updateTicket(id, updates);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKETS_DELETE')")
    @Operation(summary = "Delete ticket", description = "Delete ticket record")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
