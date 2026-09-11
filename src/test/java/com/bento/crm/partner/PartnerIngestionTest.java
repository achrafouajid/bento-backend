package com.bento.crm.partner;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PartnerIngestionTest extends IntegrationTestBase {

    private String botPayload(String externalId, String email, String phone) {
        return """
                {
                  "type": "LEAD",
                  "name": "Scraped Lead",
                  "email": "%s",
                  "phone": "%s",
                  "city": "Casablanca",
                  "country": "Morocco",
                  "source": "OTHER",
                  "stage": "NEW",
                  "external_id": "%s",
                  "source_url": "https://example.com/in/jane-doe",
                  "notes": "src:https://example.com/in/jane-doe | run:test"
                }
                """.formatted(email != null ? email : "", phone != null ? phone : "", externalId);
    }

    @Test
    void create_botLead_returnsExternalIdAndSourceUrl() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(botPayload("ext-001", "Jane@Example.COM", "+212600000001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.external_id").value("ext-001"))
                .andExpect(jsonPath("$.source_url").value("https://example.com/in/jane-doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void create_duplicateExternalId_returns409() throws Exception {
        String token = signUpAndLogin();
        String payload = botPayload("ext-dup", "dup@example.com", "+212600000002");

        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // Retry with the same external_id (bot retry) must not create a second row.
        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void create_botLeadWithoutEmailOrPhone_returns400() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(botPayload("ext-nocontact", "", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_invalidEnum_returns400Not500() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "LEAD",
                                  "name": "Bad Enum Lead",
                                  "email": "badenum@example.com",
                                  "source": "NOPE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_handEnteredLeadWithoutExternalId_stillWorks() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "LEAD",
                                  "name": "Manual Lead"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.external_id").doesNotExist());
    }
}
