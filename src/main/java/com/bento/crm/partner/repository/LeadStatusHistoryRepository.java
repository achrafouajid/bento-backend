package com.bento.crm.partner.repository;

import com.bento.crm.partner.model.LeadStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadStatusHistoryRepository extends JpaRepository<LeadStatusHistory, UUID> {

    @Query("SELECT h FROM LeadStatusHistory h WHERE h.organizationId = :orgId AND h.partnerId = :partnerId ORDER BY h.changedAt ASC")
    List<LeadStatusHistory> findByOrganizationIdAndPartnerId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId);
}
