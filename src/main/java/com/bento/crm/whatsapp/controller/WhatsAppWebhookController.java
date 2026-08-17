package com.bento.crm.whatsapp.controller;

import com.bento.crm.whatsapp.service.WhatsAppWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Meta's inbound webhook.
 *
 * <p>This endpoint is deliberately outside the JWT/tenant filter chain: Meta calls
 * it with no Authorization header. Its authentication is the
 * {@code X-Hub-Signature-256} HMAC over the raw body, which is why the body is
 * bound as {@code byte[]} — re-serializing parsed JSON would change the bytes and
 * break the signature.
 */
@RestController
@RequestMapping("/webhooks/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Webhook", description = "Inbound callbacks from Meta")
@Slf4j
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.meta.app-secret:}")
    private String appSecret;

    @Value("${whatsapp.meta.verify-token:bento-verify}")
    private String verifyToken;

    /**
     * Meta's subscription handshake: it calls once with a challenge and expects the
     * challenge echoed back as plain text.
     */
    @GetMapping
    @Operation(summary = "Webhook verification handshake")
    public ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                         @RequestParam(name = "hub.verify_token", required = false) String token,
                                         @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("[wa-hook] verification handshake accepted");
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(challenge);
        }
        log.warn("[wa-hook] verification handshake rejected (mode={})", mode);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Always answers 200, even on failure.
     *
     * <p>Meta retries aggressively on any non-200 and throttles endpoints that keep
     * failing. Since a redelivery cannot help a malformed payload, the work is done
     * behind an always-200 response and failures are logged instead.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Receive inbound messages and delivery statuses")
    public ResponseEntity<Void> receive(@RequestBody byte[] rawBody,
                                        @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        try {
            if (!signatureValid(rawBody, signature)) {
                log.warn("[wa-hook] rejected payload with invalid signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            JsonNode payload = objectMapper.readTree(rawBody);
            webhookService.handle(payload);

        } catch (Exception e) {
            log.error("[wa-hook] failed to process webhook payload", e);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Verifies Meta's HMAC-SHA256 over the raw body.
     *
     * <p>With no secret configured — the mock setup, before a Meta app exists —
     * verification is skipped so the flow is testable, and a warning is logged so
     * this cannot quietly persist into production.
     */
    private boolean signatureValid(byte[] rawBody, String signature) {
        if (appSecret == null || appSecret.isBlank()) {
            log.warn("[wa-hook] whatsapp.meta.app-secret is not set: accepting webhook without signature check");
            return true;
        }
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody);

            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }

            // Constant-time comparison: a byte-by-byte early exit would leak the
            // expected signature to a timing attack.
            return MessageDigest.isEqual(
                    hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.substring("sha256=".length()).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("[wa-hook] signature verification error", e);
            return false;
        }
    }
}
