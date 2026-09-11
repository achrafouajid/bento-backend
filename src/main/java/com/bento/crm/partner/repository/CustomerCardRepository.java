package com.bento.crm.partner.repository;

import com.bento.crm.partner.model.CustomerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerCardRepository extends JpaRepository<CustomerCard, UUID> {

    @Query("SELECT c FROM CustomerCard c WHERE c.organizationId = :orgId AND c.partnerId = :partnerId")
    Optional<CustomerCard> findByOrganizationIdAndPartnerId(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId);
}
