package com.bento.crm.whatsapp.repository;

import com.bento.crm.whatsapp.model.WaConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaConversationRepository extends JpaRepository<WaConversation, UUID> {

    @Query("SELECT c FROM WaConversation c WHERE c.organizationId = :orgId AND c.phoneE164 = :phone")
    Optional<WaConversation> findByOrgAndPhone(@Param("orgId") UUID orgId, @Param("phone") String phone);

    @Query("SELECT c FROM WaConversation c WHERE c.organizationId = :orgId AND c.id = :id")
    Optional<WaConversation> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);

    @Query("SELECT c FROM WaConversation c WHERE c.organizationId = :orgId AND c.partnerId = :partnerId")
    Optional<WaConversation> findByOrgAndPartner(@Param("orgId") UUID orgId, @Param("partnerId") UUID partnerId);
}
