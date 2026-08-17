package com.bento.crm.whatsapp.controller;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.whatsapp.model.WaAccount;
import com.bento.crm.whatsapp.repository.WaAccountRepository;
import com.bento.crm.whatsapp.service.WhatsAppWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Drives the mock transport: simulates a contact replying, or Meta reporting a
 * delivery receipt.
 *
 * <p>These endpoints build a payload in Meta's exact webhook shape and push it
 * through {@link WhatsAppWebhookService} — the same code the real callback uses.
 * Testing therefore exercises the production path (dedupe, opt-out matching,
 * relance cancellation, notification fan-out) rather than a parallel fake of it.
 *
 * <p>Refuses to run for organizations on the live META provider, so this cannot be
 * used to fabricate inbound traffic against real conversations.
 */
@RestController
@RequestMapping("/whatsapp/simulate")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Simulation", description = "Testing helpers for the mock provider")
public class WhatsAppSimulationController {

    private final WhatsAppWebhookService webhookService;
    private final WaAccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/reply")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Simulate a contact replying on WhatsApp")
    public ResponseEntity<Map<String, String>> simulateReply(@RequestBody SimulateReplyRequest request) {
        WaAccount account = requireMockAccount();

        String digits = request.getPhone() == null ? "" : request.getPhone().replaceAll("\\D", "");
        String wamid = "wamid.MOCKIN" + UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> payload = Map.of(
                "object", "whatsapp_business_account",
                "entry", List.of(Map.of(
                        "id", account.getWabaId() == null ? "mock-waba" : account.getWabaId(),
                        "changes", List.of(Map.of(
                                "field", "messages",
                                "value", Map.of(
                                        "messaging_product", "whatsapp",
                                        "metadata", Map.of(
                                                "display_phone_number", String.valueOf(account.getDisplayPhoneNumber()),
                                                "phone_number_id", account.getPhoneNumberId()),
                                        "messages", List.of(Map.of(
                                                "from", digits,
                                                "id", wamid,
                                                "timestamp", String.valueOf(Instant.now().getEpochSecond()),
                                                "type", "text",
                                                "text", Map.of("body", request.getText() == null ? "" : request.getText())))
                                )))))
        );

        webhookService.handle(objectMapper.valueToTree(payload));
        return ResponseEntity.ok(Map.of("wamid", wamid, "status", "processed"));
    }

    @PostMapping("/status")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Simulate a delivery receipt (sent, delivered, read, failed)")
    public ResponseEntity<Map<String, String>> simulateStatus(@RequestBody SimulateStatusRequest request) {
        WaAccount account = requireMockAccount();

        Map<String, Object> payload = Map.of(
                "object", "whatsapp_business_account",
                "entry", List.of(Map.of(
                        "id", "mock-waba",
                        "changes", List.of(Map.of(
                                "field", "messages",
                                "value", Map.of(
                                        "messaging_product", "whatsapp",
                                        "metadata", Map.of(
                                                "display_phone_number", String.valueOf(account.getDisplayPhoneNumber()),
                                                "phone_number_id", account.getPhoneNumberId()),
                                        "statuses", List.of(Map.of(
                                                "id", request.getWamid(),
                                                "status", request.getStatus(),
                                                "timestamp", String.valueOf(Instant.now().getEpochSecond()),
                                                "recipient_id", ""))
                                )))))
        );

        webhookService.handle(objectMapper.valueToTree(payload));
        return ResponseEntity.ok(Map.of("status", "processed"));
    }

    private WaAccount requireMockAccount() {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        WaAccount account = accountRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No WhatsApp number connected for this organization"));

        if (account.getProvider() != WaAccount.Provider.MOCK) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Simulation is only available while the organization uses the mock provider");
        }
        return account;
    }

    @Data
    public static class SimulateReplyRequest {
        private String phone;
        private String text;
    }

    @Data
    public static class SimulateStatusRequest {
        private String wamid;
        /** One of sent, delivered, read, failed. */
        private String status;
    }
}
