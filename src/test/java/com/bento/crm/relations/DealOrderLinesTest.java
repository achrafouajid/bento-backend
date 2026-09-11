package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Line items entered on a deal must be stored, read back, and replaceable — not silently dropped. */
class DealOrderLinesTest extends IntegrationTestBase {

    private String createPartner(String token) throws Exception {
        String response = mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "CUSTOMER", "name": "Lines Customer", "stage": "CUSTOMER"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void orderLines_roundTripAndReplace() throws Exception {
        String token = signUpAndLogin();
        String partnerId = createPartner(token);

        String created = mockMvc.perform(post("/deals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId": "%s", "title": "Licences", "stage": "OPEN", "amount": 30000,
                                 "orderLines": [
                                   {"product": "Licence Pro", "qty": 2, "unitPrice": 15000},
                                   {"product": "Formation", "description": "2 jours", "qty": 1, "unitPrice": 5000, "discount": 500, "total": 4500}
                                 ]}
                                """.formatted(partnerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderLines", hasSize(2)))
                // total is derived when the client does not send it
                .andExpect(jsonPath("$.orderLines[0].total").value(30000))
                .andExpect(jsonPath("$.orderLines[1].total").value(4500))
                .andReturn().getResponse().getContentAsString();
        String dealId = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/deals/" + dealId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines", hasSize(2)))
                .andExpect(jsonPath("$.orderLines[0].product").value("Licence Pro"));

        // A write without orderLines keeps them; a write with a new array replaces them.
        mockMvc.perform(patch("/deals/" + dealId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId": "%s", "title": "Licences (renamed)", "stage": "OPEN"}
                                """.formatted(partnerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines", hasSize(2)));

        mockMvc.perform(patch("/deals/" + dealId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId": "%s", "title": "Licences (renamed)", "stage": "OPEN",
                                 "orderLines": [{"product": "Support", "qty": 1, "unitPrice": 1000}]}
                                """.formatted(partnerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines", hasSize(1)))
                .andExpect(jsonPath("$.orderLines[0].product").value("Support"));

        mockMvc.perform(get("/deals").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + dealId + "')].orderLines[0].product").value("Support"));
    }
}
