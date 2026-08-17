package com.bento.crm.whatsapp.repository;

import com.bento.crm.whatsapp.model.WaFollowup;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaFollowupRepository extends JpaRepository<WaFollowup, UUID> {

    /**
     * Claims a batch of due relances across all tenants.
     *
     * <p>The lock timeout hint of {@code -2} is Hibernate's SKIP_LOCKED, which is
     * what makes this safe to run on every application instance at once: a row
     * locked by one poller is invisible to the others rather than blocking them, so
     * no two instances can pick up the same relance. That is the whole reason this
     * design needs no external claim endpoint and no distributed lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            SELECT f FROM WaFollowup f
            WHERE f.state = :state AND f.dueAt <= :now
            ORDER BY f.dueAt ASC
            """)
    List<WaFollowup> claimDueBatch(@Param("state") WaFollowup.State state,
                                   @Param("now") Instant now,
                                   Pageable pageable);

    @Query("""
            SELECT f FROM WaFollowup f
            WHERE f.organizationId = :orgId
              AND f.campaignId = :campaignId
              AND f.conversationId = :conversationId
              AND f.state = :state
            """)
    Optional<WaFollowup> findPendingForCampaignConversation(@Param("orgId") UUID orgId,
                                                            @Param("campaignId") UUID campaignId,
                                                            @Param("conversationId") UUID conversationId,
                                                            @Param("state") WaFollowup.State state);

    /**
     * Cancels every pending relance on a conversation. Called the moment an inbound
     * message lands, so a contact who answers never receives a follow-up — including
     * for other campaigns they happen to be enrolled in.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE WaFollowup f
            SET f.state = :cancelled, f.updatedAt = :now
            WHERE f.conversationId = :conversationId AND f.state = :pending
            """)
    int cancelPendingForConversation(@Param("conversationId") UUID conversationId,
                                     @Param("pending") WaFollowup.State pending,
                                     @Param("cancelled") WaFollowup.State cancelled,
                                     @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE WaFollowup f
            SET f.state = :cancelled, f.updatedAt = :now
            WHERE f.organizationId = :orgId AND f.campaignId = :campaignId AND f.state = :pending
            """)
    int cancelPendingForCampaign(@Param("orgId") UUID orgId,
                                 @Param("campaignId") UUID campaignId,
                                 @Param("pending") WaFollowup.State pending,
                                 @Param("cancelled") WaFollowup.State cancelled,
                                 @Param("now") Instant now);

    @Query("""
            SELECT COUNT(f) FROM WaFollowup f
            WHERE f.organizationId = :orgId AND f.campaignId = :campaignId AND f.state = :state
            """)
    long countForCampaignInState(@Param("orgId") UUID orgId,
                                 @Param("campaignId") UUID campaignId,
                                 @Param("state") WaFollowup.State state);

    /** Recipient id to relance due date, so the CRM can show a per-row countdown. */
    @Query("""
            SELECT f.recipientId, f.dueAt FROM WaFollowup f
            WHERE f.organizationId = :orgId AND f.campaignId = :campaignId AND f.state = :state
            """)
    List<Object[]> findDueDatesForCampaign(@Param("orgId") UUID orgId,
                                           @Param("campaignId") UUID campaignId,
                                           @Param("state") WaFollowup.State state);
}
