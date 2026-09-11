package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /campaigns is the path the Marketing page uses for email and SMS campaigns. It used to
 * fail on insert because the WhatsApp-specific NOT NULL columns were never populated.
 */
class CampaignCreationTest extends IntegrationTestBase {

    @Test
    void emailCampaign_canBeCreatedThroughTheGenericEndpoint() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Rentrée newsletter", "channel": "EMAIL", "status": "DRAFT", "sentCount": 0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Rentrée newsletter"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    @Test
    void smsCampaign_canBeCreatedThroughTheGenericEndpoint() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Rappel échéances", "channel": "SMS", "status": "SCHEDULED",
                                 "scheduledAt": "2030-01-01T09:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
}
