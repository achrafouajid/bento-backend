package com.bento.crm.automation.repository;

import com.bento.crm.automation.model.DomainEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainEventRepository extends JpaRepository<DomainEvent, UUID> {

    @Query("SELECT d FROM DomainEvent d WHERE d.organizationId = :orgId AND d.publishedAt IS NULL ORDER BY d.createdAt ASC")
    List<DomainEvent> findUnpublished(@Param("orgId") UUID orgId);

    @Query("SELECT d FROM DomainEvent d WHERE d.organizationId = :orgId")
    Page<DomainEvent> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);
}
