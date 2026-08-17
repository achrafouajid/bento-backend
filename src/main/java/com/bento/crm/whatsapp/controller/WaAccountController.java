package com.bento.crm.whatsapp.controller;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.whatsapp.model.WaAccount;
import com.bento.crm.whatsapp.repository.WaAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Connects an organization's WhatsApp sending number.
 */
@RestController
@RequestMapping("/whatsapp/account")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Account", description = "Per-organization WhatsApp number configuration")
public class WaAccountController {

    private final WaAccountRepository accountRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('CAMPAIGNS_READ')")
    @Operation(summary = "Current organization's WhatsApp connection")
    public ResponseEntity<WaAccountResponse> get() {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return accountRepository.findByOrganizationId(orgId)
                .map(a -> ResponseEntity.ok(WaAccountResponse.from(a)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Connect or update the organization's WhatsApp number")
    @Transactional
    public ResponseEntity<WaAccountResponse> connect(@RequestBody ConnectRequest request) {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        WaAccount account = accountRepository.findByOrganizationId(orgId).orElseGet(WaAccount::new);
        account.setOrganizationId(orgId);
        account.setProvider(request.getProvider() == null ? WaAccount.Provider.MOCK : request.getProvider());
        account.setPhoneNumberId(request.getPhoneNumberId());
        account.setWabaId(request.getWabaId());
        account.setDisplayPhoneNumber(request.getDisplayPhoneNumber());
        account.setStatus(WaAccount.Status.CONNECTED);

        // Secrets are only overwritten when actually supplied, so re-saving the form
        // without retyping the token does not blank it out.
        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            account.setAccessToken(request.getAccessToken());
        }
        if (request.getAppSecret() != null && !request.getAppSecret().isBlank()) {
            account.setAppSecret(request.getAppSecret());
        }
        if (request.getVerifyToken() != null && !request.getVerifyToken().isBlank()) {
            account.setVerifyToken(request.getVerifyToken());
        }

        return ResponseEntity.ok(WaAccountResponse.from(accountRepository.save(account)));
    }

    /**
     * Provisions a simulated number in one click so campaigns can be exercised
     * before a Meta account exists.
     */
    @PostMapping("/mock")
    @PreAuthorize("hasAuthority('CAMPAIGNS_WRITE')")
    @Operation(summary = "Provision a simulated WhatsApp number for testing")
    @Transactional
    public ResponseEntity<WaAccountResponse> connectMock() {
        UUID orgId = TenantContext.getCurrentOrganizationId();

        WaAccount account = accountRepository.findByOrganizationId(orgId).orElseGet(WaAccount::new);
        account.setOrganizationId(orgId);
        account.setProvider(WaAccount.Provider.MOCK);
        // Derived from the org id so it stays unique across tenants, satisfying the
        // global uniqueness the webhook routing depends on.
        account.setPhoneNumberId("mock-" + orgId);
        account.setDisplayPhoneNumber("+212600000000");
        account.setStatus(WaAccount.Status.CONNECTED);

        return ResponseEntity.ok(WaAccountResponse.from(accountRepository.save(account)));
    }

    @Data
    public static class ConnectRequest {
        private WaAccount.Provider provider;
        private String phoneNumberId;
        private String wabaId;
        private String displayPhoneNumber;
        private String accessToken;
        private String appSecret;
        private String verifyToken;
    }

    /** Secrets are never echoed back; only whether they are set. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaAccountResponse {
        private UUID id;
        private String provider;
        private String phoneNumberId;
        private String wabaId;
        private String displayPhoneNumber;
        private String status;
        private String qualityRating;
        private boolean hasAccessToken;

        static WaAccountResponse from(WaAccount a) {
            return WaAccountResponse.builder()
                    .id(a.getId())
                    .provider(a.getProvider().name())
                    .phoneNumberId(a.getPhoneNumberId())
                    .wabaId(a.getWabaId())
                    .displayPhoneNumber(a.getDisplayPhoneNumber())
                    .status(a.getStatus().name())
                    .qualityRating(a.getQualityRating())
                    .hasAccessToken(a.getAccessToken() != null && !a.getAccessToken().isBlank())
                    .build();
        }
    }
}
