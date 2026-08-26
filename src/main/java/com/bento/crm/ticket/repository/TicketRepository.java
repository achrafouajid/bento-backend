package com.bento.crm.ticket.repository;

import com.bento.crm.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    @Query("SELECT t FROM Ticket t WHERE t.organizationId = :orgId")
    Page<Ticket> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.organizationId = :orgId AND t.id = :id")
    Optional<Ticket> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);
}
