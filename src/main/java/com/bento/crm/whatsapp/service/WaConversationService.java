package com.bento.crm.whatsapp.service;

import com.bento.crm.whatsapp.model.WaConversation;
import com.bento.crm.whatsapp.repository.WaConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Owns conversation lifecycle and the 24-hour customer service window.
 */
@Service
@RequiredArgsConstructor
public class WaConversationService {

    /** WhatsApp's customer service window: free-form text is only allowed inside it. */
    public static final Duration SERVICE_WINDOW = Duration.ofHours(24);

    private final WaConversationRepository conversationRepository;

    /**
     * Finds or creates the conversation for a phone number.
     *
     * <p>The insert races against the inbound webhook, which can create the same
     * conversation for the same number at the same moment. The unique constraint on
     * (organization_id, phone_e164) is what actually decides the winner; the loser
     * re-reads rather than failing the send.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WaConversation getOrCreate(UUID organizationId, String phoneE164, UUID partnerId) {
        return conversationRepository.findByOrgAndPhone(organizationId, phoneE164)
                .map(existing -> {
                    if (existing.getPartnerId() == null && partnerId != null) {
                        existing.setPartnerId(partnerId);
                        return conversationRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    WaConversation created = new WaConversation();
                    created.setOrganizationId(organizationId);
                    created.setPhoneE164(phoneE164);
                    created.setPartnerId(partnerId);
                    try {
                        return conversationRepository.saveAndFlush(created);
                    } catch (DataIntegrityViolationException e) {
                        return conversationRepository.findByOrgAndPhone(organizationId, phoneE164)
                                .orElseThrow(() -> e);
                    }
                });
    }

    @Transactional
    public void recordOutbound(UUID conversationId, Instant at) {
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.setLastOutboundAt(at);
            conversationRepository.save(c);
        });
    }

    /**
     * Records an inbound message and reopens the 24-hour window.
     *
     * <p>Only an inbound message extends the window — outbound traffic never does,
     * which is why a campaign send alone leaves the tenant restricted to templates.
     */
    @Transactional
    public void recordInbound(UUID conversationId, Instant at) {
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.setLastInboundAt(at);
            c.setWindowExpiresAt(at.plus(SERVICE_WINDOW));
            conversationRepository.save(c);
        });
    }

    @Transactional
    public void optOut(UUID conversationId, Instant at) {
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.setOptedOutAt(at);
            conversationRepository.save(c);
        });
    }
}
