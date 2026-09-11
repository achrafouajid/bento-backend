package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The ticket form's category and partner link must survive a round trip through the API. */
class TicketFieldsTest extends IntegrationTestBase {

    @Test
    void ticketType_isStoredAndReturned() throws Exception {
        String token = signUpAndLogin();

        String response = mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Invoice PDF broken", "status": "OPEN", "type": "Billing issue"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Billing issue"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/tickets/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Billing issue"));
    }
}
