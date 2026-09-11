package com.bento.crm.ticket.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.common.model.EntityLink;
import com.bento.crm.common.model.RelatedEntityType;
import com.bento.crm.common.repository.EntityLinkSpecifications;
import com.bento.crm.notification.event.AssignmentNotificationFactory;
import com.bento.crm.ticket.dto.CreateTicketRequest;
import com.bento.crm.ticket.model.Ticket;
import com.bento.crm.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        applyRequest(ticket, request);
        UUID orgId = TenantContext.getCurrentOrganizationId();
        ticket.setOrganizationId(orgId);
        Ticket saved = ticketRepository.save(ticket);
        notifyIfAssigned(orgId, null, saved);
        return saved;
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

    /**
     * Lists the tickets attached to a given record, e.g. everything opened against one customer.
     * Both filter arguments are optional; with neither set this is equivalent to
     * {@link #listTickets(Pageable)}.
     */
    public Page<Ticket> listTickets(RelatedEntityType relatedEntityType, UUID relatedEntityId, Pageable pageable) {
        if (relatedEntityType == null && relatedEntityId == null) {
            return listTickets(pageable);
        }
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Specification<Ticket> spec = EntityLinkSpecifications.<Ticket>inOrganization(orgId)
                .and(EntityLinkSpecifications.relatedTo(relatedEntityType, relatedEntityId));
        return ticketRepository.findAll(spec, pageable);
    }

    @Transactional
    public Ticket updateTicket(UUID id, CreateTicketRequest request) {
        Ticket ticket = getTicket(id);
        UUID previousAssignee = ticket.getAssignedToUserId();
        applyRequest(ticket, request);
        Ticket saved = ticketRepository.save(ticket);
        notifyIfAssigned(saved.getOrganizationId(), previousAssignee, saved);
        return saved;
    }

    private void notifyIfAssigned(UUID orgId, UUID previousAssignee, Ticket ticket) {
        UUID current = ticket.getAssignedToUserId();
        if (current == null || Objects.equals(current, previousAssignee)) {
            return;
        }
        eventPublisher.publishEvent(AssignmentNotificationFactory.forTicket(
                orgId, current, currentActor(), ticket.getId(), ticket.getTitle()));
    }

    private static UUID currentActor() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                    ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                    : null;
            return principal instanceof String s ? UUID.fromString(s) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void applyRequest(Ticket ticket, CreateTicketRequest request) {
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setRelatedEntity(resolveLink(request));
        ticket.setPartnerId(ticket.getRelatedEntity().idOf(RelatedEntityType.PARTNER));
        ticket.setAssignedToUserId(request.getAssignedToUserId());
        ticket.setStatus(request.getStatus());
        ticket.setPriority(request.getPriority());
        ticket.setDeadline(request.getDeadline());
        ticket.setResolution(request.getResolution());
    }

    /**
     * Reads the ticket's optional link, falling back to the legacy {@code partnerId} shorthand so
     * clients that only know about the partner field keep behaving exactly as before.
     */
    private EntityLink resolveLink(CreateTicketRequest request) {
        EntityLink link = request.toEntityLink();
        return link.isPresent() ? link : EntityLink.of(RelatedEntityType.PARTNER, request.getPartnerId());
    }

    @Transactional
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicket(id);
        ticketRepository.delete(ticket);
    }
}
