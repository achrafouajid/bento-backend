package com.bento.crm.whatsapp.provider;

import com.bento.crm.whatsapp.model.WaAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Simulated transport, so the full campaign → relance → reply → notification loop
 * can be exercised without a Meta account, business verification, or approved
 * templates.
 *
 * <p>Sends succeed and return a synthetic {@code wamid}. Delivery and read receipts
 * that Meta would push asynchronously are simulated by
 * {@code MockWhatsAppSimulator}, and inbound replies can be triggered from the
 * simulation endpoint.
 *
 * <p>Two phone-number conventions make the failure branches reachable without
 * special configuration:
 * <ul>
 *   <li>a number ending in {@code 0000} fails permanently with 131026
 *       (not a WhatsApp user)</li>
 *   <li>a number ending in {@code 9999} fails retryably with 80007 (rate limited)</li>
 * </ul>
 */
@Component
@Slf4j
public class MockWhatsAppProvider implements WhatsAppProvider {

    @Override
    public WaAccount.Provider kind() {
        return WaAccount.Provider.MOCK;
    }

    @Override
    public SendResult sendTemplate(WaAccount account, String toPhoneE164, String templateName,
                                   String languageCode, List<String> params) {
        log.info("[mock-wa] template '{}' ({}) -> {} params={}", templateName, languageCode, toPhoneE164, params);
        return simulate(toPhoneE164);
    }

    @Override
    public SendResult sendText(WaAccount account, String toPhoneE164, String body) {
        log.info("[mock-wa] text -> {}: {}", toPhoneE164, body);
        return simulate(toPhoneE164);
    }

    private SendResult simulate(String toPhoneE164) {
        String digits = toPhoneE164 == null ? "" : toPhoneE164.replaceAll("\\D", "");
        if (digits.endsWith("0000")) {
            return SendResult.permanentFailure("131026", "Message undeliverable: recipient is not a WhatsApp user");
        }
        if (digits.endsWith("9999")) {
            return SendResult.retryableFailure("80007", "Rate limit hit");
        }
        return SendResult.ok("wamid.MOCK" + UUID.randomUUID().toString().replace("-", ""));
    }
}
