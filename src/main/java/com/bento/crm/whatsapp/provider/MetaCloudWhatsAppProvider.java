package com.bento.crm.whatsapp.provider;

import com.bento.crm.whatsapp.model.WaAccount;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Meta WhatsApp Cloud API transport.
 *
 * <p>Credentials are read from the tenant's {@link WaAccount} rather than global
 * configuration, so each organization sends from its own number with its own
 * system-user token.
 */
@Component
@Slf4j
public class MetaCloudWhatsAppProvider implements WhatsAppProvider {

    /**
     * Errors where a later retry can plausibly succeed. Everything else is treated
     * as permanent so a dead number is not retried forever: 131026 means the number
     * is not on WhatsApp, 132000/132001 mean the template does not exist or its
     * parameter count does not match, and 131047 means free-form was attempted
     * outside the 24-hour window.
     */
    private static final Set<String> RETRYABLE_CODES = Set.of("80007", "130429", "131000", "131056", "133016");

    private final RestClient restClient;
    private final String apiVersion;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public MetaCloudWhatsAppProvider(RestClient.Builder builder,
                                     com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                                     @Value("${whatsapp.meta.base-url:https://graph.facebook.com}") String baseUrl,
                                     @Value("${whatsapp.meta.api-version:v22.0}") String apiVersion) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiVersion = apiVersion;
    }

    @Override
    public WaAccount.Provider kind() {
        return WaAccount.Provider.META;
    }

    @Override
    public SendResult sendTemplate(WaAccount account, String toPhoneE164, String templateName,
                                   String languageCode, List<String> params) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        if (params != null) {
            for (String p : params) {
                parameters.add(Map.of("type", "text", "text", p == null ? "" : p));
            }
        }

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode == null ? "fr" : languageCode));
        if (!parameters.isEmpty()) {
            template.put("components", List.of(Map.of("type", "body", "parameters", parameters)));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", toE164Digits(toPhoneE164));
        payload.put("type", "template");
        payload.put("template", template);

        return post(account, payload);
    }

    @Override
    public SendResult sendText(WaAccount account, String toPhoneE164, String body) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", toE164Digits(toPhoneE164));
        payload.put("type", "text");
        payload.put("text", Map.of("preview_url", false, "body", body));
        return post(account, payload);
    }

    private SendResult post(WaAccount account, Map<String, Object> payload) {
        String path = "/" + apiVersion + "/" + account.getPhoneNumberId() + "/messages";
        try {
            JsonNode response = restClient.post()
                    .uri(path)
                    .header("Authorization", "Bearer " + account.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            String wamid = response != null
                    ? response.path("messages").path(0).path("id").asText(null)
                    : null;

            if (wamid == null) {
                log.warn("[meta-wa] send accepted but no wamid returned: {}", response);
                return SendResult.retryableFailure("NO_WAMID", "Meta accepted the call but returned no message id");
            }
            return SendResult.ok(wamid);

        } catch (org.springframework.web.client.RestClientResponseException e) {
            return mapError(e);
        } catch (Exception e) {
            // Transport-level failure (DNS, timeout, connection reset): always worth a retry.
            log.warn("[meta-wa] transport failure calling {}", path, e);
            return SendResult.retryableFailure("TRANSPORT", e.getMessage());
        }
    }

    private SendResult mapError(org.springframework.web.client.RestClientResponseException e) {
        String code = "HTTP_" + e.getStatusCode().value();
        String title = e.getResponseBodyAsString();
        try {
            JsonNode error = objectMapper.readTree(e.getResponseBodyAsString()).path("error");
            if (!error.isMissingNode()) {
                code = error.path("code").asText(code);
                title = error.path("error_data").path("details").asText(
                        error.path("message").asText(title));
            }
        } catch (Exception ignored) {
            // Fall back to the raw body already captured above.
        }

        if (title != null && title.length() > 500) {
            title = title.substring(0, 500);
        }

        boolean retryable = RETRYABLE_CODES.contains(code) || e.getStatusCode().is5xxServerError();
        log.warn("[meta-wa] send failed code={} retryable={} title={}", code, retryable, title);
        return retryable ? SendResult.retryableFailure(code, title) : SendResult.permanentFailure(code, title);
    }

    /** Meta's {@code to} field wants digits only, no leading plus. */
    private static String toE164Digits(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }
}
