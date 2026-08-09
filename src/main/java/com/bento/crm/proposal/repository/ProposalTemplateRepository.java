package com.bento.crm.proposal.repository;

import com.bento.crm.proposal.model.ProposalTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalTemplateRepository extends JpaRepository<ProposalTemplate, UUID> {

    @Query("SELECT t FROM ProposalTemplate t WHERE t.organizationId = :organizationId")
    Page<ProposalTemplate> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT t FROM ProposalTemplate t WHERE t.organizationId = :organizationId AND t.id = :id")
    Optional<ProposalTemplate> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
