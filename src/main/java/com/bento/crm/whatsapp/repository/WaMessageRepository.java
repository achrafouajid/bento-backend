package com.bento.crm.whatsapp.repository;

import com.bento.crm.whatsapp.model.WaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaMessageRepository extends JpaRepository<WaMessage, UUID> {

    /** Idempotency lookup: Meta redelivers webhooks, so every handler dedupes on wamid. */
    Optional<WaMessage> findByWamid(String wamid);

    boolean existsByWamid(String wamid);

    @Query("""
            SELECT m FROM WaMessage m
            WHERE m.organizationId = :orgId AND m.conversationId = :conversationId
            ORDER BY m.createdAt ASC
            """)
    List<WaMessage> findConversationTimeline(@Param("orgId") UUID orgId,
                                             @Param("conversationId") UUID conversationId);
}
