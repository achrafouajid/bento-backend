package com.bento.crm.deal.repository;

import com.bento.crm.deal.model.DealActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealActivityRepository extends JpaRepository<DealActivity, UUID> {

    @Query("SELECT a FROM DealActivity a WHERE a.organizationId = :orgId AND a.dealId = :dealId ORDER BY a.occurredAt DESC")
    List<DealActivity> findByOrganizationIdAndDealId(@Param("orgId") UUID orgId, @Param("dealId") UUID dealId);

    @Query("SELECT a FROM DealActivity a WHERE a.organizationId = :orgId AND a.dealId = :dealId AND a.id = :id")
    Optional<DealActivity> findByOrganizationIdAndDealIdAndId(@Param("orgId") UUID orgId, @Param("dealId") UUID dealId, @Param("id") UUID id);
}
