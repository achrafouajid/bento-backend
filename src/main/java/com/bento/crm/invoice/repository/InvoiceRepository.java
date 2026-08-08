package com.bento.crm.invoice.repository;

import com.bento.crm.invoice.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("SELECT i FROM Invoice i WHERE i.organizationId = :orgId")
    Page<Invoice> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.organizationId = :orgId AND i.id = :id")
    Optional<Invoice> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);

    @Query("SELECT i FROM Invoice i WHERE i.organizationId = :orgId AND i.status = :status")
    Page<Invoice> findByOrganizationIdAndStatus(@Param("orgId") UUID orgId, @Param("status") Invoice.Status status, Pageable pageable);
}
