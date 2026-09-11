package com.bento.crm.partner.repository;

import com.bento.crm.partner.model.LeadContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadContactRepository extends JpaRepository<LeadContact, UUID> {

    @Query("SELECT c FROM LeadContact c WHERE c.organizationId = :orgId AND c.partnerId = :partnerId ORDER BY c.createdAt ASC")
    List<LeadContact> findByOrganizationIdAndPartnerId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId);

    @Query("SELECT c FROM LeadContact c WHERE c.organizationId = :orgId AND c.partnerId = :partnerId AND c.id = :id")
    Optional<LeadContact> findByOrganizationIdAndPartnerIdAndId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId, @Param("id") UUID id);
}
