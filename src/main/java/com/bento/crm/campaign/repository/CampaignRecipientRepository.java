package com.bento.crm.campaign.repository;

import com.bento.crm.campaign.model.CampaignRecipient;
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
public interface CampaignRecipientRepository extends JpaRepository<CampaignRecipient, UUID> {

    @Query("SELECT r FROM CampaignRecipient r WHERE r.organizationId = :orgId AND r.campaignId = :campaignId")
    Page<CampaignRecipient> findByCampaign(@Param("orgId") UUID orgId,
                                           @Param("campaignId") UUID campaignId,
                                           Pageable pageable);

    @Query("SELECT r FROM CampaignRecipient r WHERE r.organizationId = :orgId AND r.campaignId = :campaignId")
    List<CampaignRecipient> findAllByCampaign(@Param("orgId") UUID orgId, @Param("campaignId") UUID campaignId);

    @Query("SELECT r FROM CampaignRecipient r WHERE r.organizationId = :orgId AND r.id = :id")
    Optional<CampaignRecipient> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);

    /**
     * All recipients tied to a conversation. An inbound reply arrives on the
     * conversation, not on a campaign, so it may need to mark the contact as having
     * replied across several campaigns at once.
     */
    @Query("SELECT r FROM CampaignRecipient r WHERE r.conversationId = :conversationId")
    List<CampaignRecipient> findByConversation(@Param("conversationId") UUID conversationId);

    @Query("""
            SELECT r.status, COUNT(r) FROM CampaignRecipient r
            WHERE r.organizationId = :orgId AND r.campaignId = :campaignId
            GROUP BY r.status
            """)
    List<Object[]> countByStatusForCampaign(@Param("orgId") UUID orgId, @Param("campaignId") UUID campaignId);

    @Query("""
            SELECT COUNT(r) FROM CampaignRecipient r
            WHERE r.organizationId = :orgId AND r.status = :status
            """)
    long countForOrgInStatus(@Param("orgId") UUID orgId,
                             @Param("status") CampaignRecipient.Status status);
}
