package com.bento.crm.partner.repository;

import com.bento.crm.partner.model.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {

    @Query("SELECT p FROM Partner p WHERE p.organizationId = :orgId AND p.type = :type")
    Page<Partner> findByOrganizationIdAndType(@Param("orgId") UUID orgId, @Param("type") Partner.PartnerType type, Pageable pageable);

    @Query("SELECT p FROM Partner p WHERE p.organizationId = :orgId AND p.stage = :stage")
    Page<Partner> findByOrganizationIdAndStage(@Param("orgId") UUID orgId, @Param("stage") Partner.PartnerStage stage, Pageable pageable);

    @Query("SELECT p FROM Partner p WHERE p.organizationId = :orgId AND p.id = :id")
    Optional<Partner> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);

    @Query("SELECT p FROM Partner p WHERE p.organizationId = :orgId ORDER BY p.createdAt DESC")
    Page<Partner> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);
}
