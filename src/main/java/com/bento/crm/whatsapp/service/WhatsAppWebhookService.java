package com.bento.crm.whatsapp.service;

import com.bento.crm.campaign.model.Campaign;
import com.bento.crm.campaign.model.CampaignRecipient;
import com.bento.crm.campaign.repository.CampaignRecipientRepository;
import com.bento.crm.campaign.repository.CampaignRepository;
import com.bento.crm.notification.model.Notification;
import com.bento.crm.notification.service.NotificationService;
import com.bento.crm.partner.model.Partner;
import com.bento.crm.partner.repository.PartnerRepository;
import com.bento.crm.whatsapp.model.WaAccount;
import com.bento.crm.whatsapp.model.WaConversation;
import com.bento.crm.whatsapp.model.WaMessage;
import com.bento.crm.whatsapp.repository.WaAccountRepository;
import com.bento.crm.whatsapp.repository.WaMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handles Meta's inbound webhook: replies, delivery receipts and opt-outs.
 *
 * <p>Tenancy is the subtle part. The callback arrives unauthenticated and carries
 * no organization id — only {@code metadata.phone_number_id}. Every handler here
 * therefore resolves the tenant from {@link WaAccount} and then passes the
 * organization id explicitly, because the usual {@code TenantContext} ThreadLocal
 * is never populated on this request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookService {

    /** A contact typing any of these is a legal opt-out and must be honoured immediately. */
    private static final Pattern OPT_OUT = Pattern.compile(
            "^\\s*(stop|stop\\s*pub|arret|arr[êe]t|d[ée]sabonner|desabonnement|unsubscribe|cancel)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private final WaAccountRepository accountRepository;
    private final WaMessageRepository messageRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final PartnerRepository partnerRepository;
    private final WaConversationService conversationService;
    private final WaFollowupService followupService;
    private final NotificationService notificationService;

    /**
     * Processes one webhook body. Meta batches several entries and several changes
     * per delivery, so this flattens before dispatching.
     */
    public void handle(JsonNode payload) {
        for (JsonNode entry : payload.path("entry")) {
            for (JsonNode change : entry.path("changes")) {
                JsonNode value = change.path("value");
                String phoneNumberId = value.path("metadata").path("phone_number_id").asText(null);

                Optional<WaAccount> account = phoneNumberId == null
                        ? Optional.empty()
                        : accountRepository.findByPhoneNumberId(phoneNumberId);

                if (account.isEmpty()) {
                    log.warn("[wa-hook] no account for phone_number_id={}, ignoring", phoneNumberId);
                    continue;
                }

                UUID orgId = account.get().getOrganizationId();
                value.path("messages").forEach(m -> safely(() -> handleInboundMessage(orgId, m)));
                value.path("statuses").forEach(s -> safely(() -> handleStatus(orgId, s)));
            }
        }
    }

    /**
     * One malformed item must not abort the rest of the batch — Meta would retry the
     * whole delivery and the good items would be reprocessed.
     */
    private void safely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("[wa-hook] failed to process webhook item", e);
        }
    }

    @Transactional
    public void handleInboundMessage(UUID orgId, JsonNode message) {
        String wamid = message.path("id").asText(null);
        if (wamid == null) {
            return;
        }

        // Meta redelivers aggressively on any non-200. wamid is the idempotency key
        // that makes a redelivered reply a no-op rather than a duplicate timeline
        // entry and a second notification.
        if (messageRepository.existsByWamid(wamid)) {
            log.debug("[wa-hook] duplicate inbound {}, ignoring", wamid);
            return;
        }

        String from = "+" + message.path("from").asText("").replaceAll("\\D", "");
        String type = message.path("type").asText("text");
        String body = extractBody(message, type);
        Instant occurredAt = parseTimestamp(message.path("timestamp").asText(null));

        Partner partner = partnerRepository
                .findByOrganizationIdAndPhoneDigits(orgId, from.replaceAll("\\D", ""))
                .orElse(null);

        WaConversation conversation = conversationService.getOrCreate(
                orgId, from, partner == null ? null : partner.getId());

        WaMessage inbound = new WaMessage();
        inbound.setOrganizationId(orgId);
        inbound.setConversationId(conversation.getId());
        inbound.setDirection(WaMessage.Direction.IN);
        inbound.setWamid(wamid);
        inbound.setMessageType(type);
        inbound.setBody(body);
        inbound.setStatus(WaMessage.Status.RECEIVED);
        messageRepository.save(inbound);

        conversationService.recordInbound(conversation.getId(), occurredAt);

        if (body != null && OPT_OUT.matcher(body).matches()) {
            handleOptOut(orgId, conversation, occurredAt);
            return;
        }

        // A reply cancels every pending relance on this conversation, including for
        // other campaigns the contact happens to be enrolled in — someone who
        // answered should not then be chased by an unrelated sequence.
        int cancelled = followupService.cancelForConversation(conversation.getId());

        List<CampaignRecipient> recipients = recipientRepository.findByConversation(conversation.getId());
        for (CampaignRecipient recipient : recipients) {
            if (recipient.canAdvanceTo(CampaignRecipient.Status.REPLIED)) {
                recipient.setStatus(CampaignRecipient.Status.REPLIED);
                recipient.setRepliedAt(occurredAt);
                recipientRepository.save(recipient);
            }
        }
        recipientRepository.flush();

        // Cancelling this contact's relance may have been the last one outstanding,
        // which finishes the campaign.
        recipients.stream()
                .map(CampaignRecipient::getCampaignId)
                .distinct()
                .forEach(campaignId -> followupService.completeCampaignIfDone(orgId, campaignId));

        notifyAssignedUser(orgId, partner, conversation, body, recipients);

        log.info("[wa-hook] inbound from {} ({} relance(s) cancelled, {} recipient(s) marked replied)",
                from, cancelled, recipients.size());
    }

    private void handleOptOut(UUID orgId, WaConversation conversation, Instant at) {
        conversationService.optOut(conversation.getId(), at);
        followupService.cancelForConversation(conversation.getId());

        for (CampaignRecipient recipient : recipientRepository.findByConversation(conversation.getId())) {
            if (recipient.canAdvanceTo(CampaignRecipient.Status.OPTED_OUT)) {
                recipient.setStatus(CampaignRecipient.Status.OPTED_OUT);
                recipient.setErrorCode("OPTED_OUT");
                recipient.setErrorTitle("Contact sent STOP");
                recipientRepository.save(recipient);
            }
        }
        log.info("[wa-hook] opt-out recorded for conversation {}", conversation.getId());
    }

    /**
     * Applies a delivery receipt to the outbound message and its campaign recipient.
     */
    @Transactional
    public void handleStatus(UUID orgId, JsonNode status) {
        String wamid = status.path("id").asText(null);
        String state = status.path("status").asText("");
        if (wamid == null) {
            return;
        }

        WaMessage message = messageRepository.findByWamid(wamid).orElse(null);
        if (message == null) {
            log.debug("[wa-hook] status for unknown wamid {}", wamid);
            return;
        }

        Instant at = parseTimestamp(status.path("timestamp").asText(null));

        switch (state) {
            case "sent" -> message.setStatus(WaMessage.Status.SENT);
            case "delivered" -> message.setStatus(WaMessage.Status.DELIVERED);
            case "read" -> message.setStatus(WaMessage.Status.READ);
            case "failed" -> {
                message.setStatus(WaMessage.Status.FAILED);
                JsonNode error = status.path("errors").path(0);
                message.setErrorCode(error.path("code").asText(null));
                message.setErrorTitle(error.path("title").asText(null));
            }
            default -> {
                return;
            }
        }
        messageRepository.save(message);

        if (message.getRecipientId() == null) {
            return;
        }
        recipientRepository.findById(message.getRecipientId()).ifPresent(recipient -> {
            CampaignRecipient.Status next = switch (state) {
                case "delivered" -> CampaignRecipient.Status.DELIVERED;
                case "read" -> CampaignRecipient.Status.READ;
                case "failed" -> CampaignRecipient.Status.FAILED;
                default -> CampaignRecipient.Status.SENT;
            };
            // Meta's status callbacks are not ordered: a 'delivered' can land after a
            // 'read'. canAdvanceTo keeps the CRM from walking a recipient backwards.
            if (!recipient.canAdvanceTo(next)) {
                return;
            }
            recipient.setStatus(next);
            switch (next) {
                case DELIVERED -> recipient.setDeliveredAt(at);
                case READ -> recipient.setReadAt(at);
                case FAILED -> {
                    recipient.setFailedAt(at);
                    recipient.setErrorCode(message.getErrorCode());
                    recipient.setErrorTitle(message.getErrorTitle());
                }
                default -> recipient.setSentAt(at);
            }
            recipientRepository.save(recipient);
        });
    }

    /**
     * Notifies the CRM user who should see the reply.
     *
     * <p>Preference order is assignee, then owner, then whoever launched the
     * campaign. The last fallback matters more than it looks: contacts imported in
     * bulk routinely have no assignee, and without it a reply — the entire point of
     * the campaign — would be recorded and silently shown to nobody.
     */
    private void notifyAssignedUser(UUID orgId, Partner partner, WaConversation conversation,
                                    String body, List<CampaignRecipient> recipients) {
        UUID recipientUserId = partner != null ? partner.getAssignedToUserId() : null;
        if (recipientUserId == null && partner != null) {
            recipientUserId = partner.getOwnerId();
        }
        if (recipientUserId == null) {
            recipientUserId = recipients.stream()
                    .map(r -> campaignRepository.findByOrganizationIdAndId(orgId, r.getCampaignId()).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .map(Campaign::getCreatedBy)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        if (recipientUserId == null) {
            log.warn("[wa-hook] inbound on conversation {} has no assignee, owner or campaign creator: "
                    + "no one will be notified", conversation.getId());
            return;
        }

        String who = partner != null ? partner.getName() : conversation.getPhoneE164();
        String preview = body == null ? "(no text)" : (body.length() > 160 ? body.substring(0, 160) + "…" : body);

        Notification notification = new Notification();
        notification.setOrganizationId(orgId);
        notification.setRecipientUserId(recipientUserId);
        notification.setType(Notification.NotificationType.WHATSAPP);
        notification.setTitle(who + " replied on WhatsApp");
        notification.setMessage(preview);
        notification.setRelatedEntityType("WA_CONVERSATION");
        notification.setRelatedEntityId(conversation.getId());
        notification.setIsRead(false);

        notificationService.createForOrganization(orgId, notification);
    }

    /** Extracts displayable text across the message types a campaign realistically sees. */
    private String extractBody(JsonNode message, String type) {
        return switch (type) {
            case "text" -> message.path("text").path("body").asText(null);
            // Quick-reply and call-to-action button taps arrive as their own types.
            // Handling them matters because "did they respond?" is far more reliable
            // to detect from a button tap than from parsing free text.
            case "button" -> message.path("button").path("text").asText(null);
            case "interactive" -> {
                JsonNode interactive = message.path("interactive");
                yield interactive.path("button_reply").path("title").asText(
                        interactive.path("list_reply").path("title").asText(null));
            }
            case "image", "video", "document", "audio" ->
                    message.path(type).path("caption").asText("[" + type + "]");
            case "location" -> "[location]";
            case "sticker" -> "[sticker]";
            // Default case is not optional: without it an unhandled type stores null
            // and the CRM timeline renders an empty bubble.
            default -> "[" + type + "]";
        };
    }

    private Instant parseTimestamp(String epochSeconds) {
        if (epochSeconds == null || epochSeconds.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(epochSeconds));
        } catch (NumberFormatException e) {
            return Instant.now();
        }
    }
}
