package com.bento.crm.ticket.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.ticket.dto.CreateTicketRequest;
import com.bento.crm.ticket.model.Ticket;
import com.bento.crm.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        applyRequest(ticket, request);
        ticket.setOrganizationId(TenantContext.getCurrentOrganizationId());
        return ticketRepository.save(ticket);
    }

    public Ticket getTicket(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return ticketRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    public Page<Ticket> listTickets(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return ticketRepository.findByOrganizationId(orgId, pageable);
    }

    @Transactional
    public Ticket updateTicket(UUID id, CreateTicketRequest request) {
        Ticket ticket = getTicket(id);
        applyRequest(ticket, request);
        return ticketRepository.save(ticket);
    }

    private void applyRequest(Ticket ticket, CreateTicketRequest request) {
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPartnerId(request.getPartnerId());
        ticket.setAssignedToUserId(request.getAssignedToUserId());
        ticket.setStatus(request.getStatus());
        ticket.setPriority(request.getPriority());
        ticket.setDeadline(request.getDeadline());
        ticket.setResolution(request.getResolution());
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicket(id);
        ticketRepository.delete(ticket);
    }
}
