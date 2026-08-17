package com.bento.crm.partner.repository;

import com.bento.crm.partner.model.LeadActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadActivityRepository extends JpaRepository<LeadActivity, UUID> {

    @Query("SELECT a FROM LeadActivity a WHERE a.organizationId = :orgId AND a.partnerId = :partnerId ORDER BY a.occurredAt DESC")
    List<LeadActivity> findByOrganizationIdAndPartnerId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId);

    @Query("SELECT a FROM LeadActivity a WHERE a.organizationId = :orgId AND a.partnerId = :partnerId AND a.id = :id")
    Optional<LeadActivity> findByOrganizationIdAndPartnerIdAndId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId, @Param("id") UUID id);
}
