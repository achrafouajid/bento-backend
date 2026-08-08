package com.bento.crm.ticket.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
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
    public Ticket createTicket(Ticket ticket) {
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
    public Ticket updateTicket(UUID id, Ticket updates) {
        Ticket ticket = getTicket(id);
        ticket.setTitle(updates.getTitle());
        ticket.setStatus(updates.getStatus());
        ticket.setPriority(updates.getPriority());
        ticket.setAssignedToUserId(updates.getAssignedToUserId());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicket(id);
        ticketRepository.delete(ticket);
    }
}
