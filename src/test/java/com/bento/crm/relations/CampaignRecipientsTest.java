package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers audience-building on a campaign's detail page: enrolling contacts (by individual pick,
 * by type, or by a stage-based group — all of which resolve to a plain partner-id list before
 * hitting this endpoint), resolving the right contact detail per channel, and removing a
 * recipient.
 */
class CampaignRecipientsTest extends IntegrationTestBase {

    private String createPartner(String token, String name, String email, String phone) throws Exception {
        String response = mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "CUSTOMER", "name": "%s", "stage": "CUSTOMER", "email": %s, "phone": %s}
                                """.formatted(name,
                                email == null ? "null" : "\"" + email + "\"",
                                phone == null ? "null" : "\"" + phone + "\"")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createCampaign(String token, String channel) throws Exception {
        String response = mockMvc.perform(post("/campaigns")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Audience test", "channel": "%s", "status": "DRAFT", "sentCount": 0}
                                """.formatted(channel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void emailCampaign_resolvesEmailAndSkipsContactsWithout() throws Exception {
        String token = signUpAndLogin();
        String campaignId = createCampaign(token, "EMAIL");
        String withEmail = createPartner(token, "Has Email", "contact@example.com", null);
        String withoutEmail = createPartner(token, "No Email", null, "+212600000000");

        mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\", \"%s\"]}".formatted(withEmail, withoutEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withEmail + "')].email").value("contact@example.com"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withEmail + "')].status").value("PENDING"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withoutEmail + "')].status").value("SKIPPED"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withoutEmail + "')].errorCode").value("NO_EMAIL"));
    }

    @Test
    void whatsAppCampaign_resolvesPhoneAndSkipsContactsWithout() throws Exception {
        String token = signUpAndLogin();
        String campaignId = createCampaign(token, "WHATSAPP");
        String withPhone = createPartner(token, "Has Phone", "a@example.com", "+212600000001");
        String withoutPhone = createPartner(token, "No Phone", "b@example.com", null);

        mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\", \"%s\"]}".formatted(withPhone, withoutPhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.partnerId == '" + withPhone + "')].phone").value("+212600000001"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withPhone + "')].status").value("PENDING"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withoutPhone + "')].status").value("SKIPPED"))
                .andExpect(jsonPath("$[?(@.partnerId == '" + withoutPhone + "')].errorCode").value("NO_PHONE"));
    }

    @Test
    void reEnrollingTheSamePartner_isANoOpNotADuplicate() throws Exception {
        String token = signUpAndLogin();
        String campaignId = createCampaign(token, "EMAIL");
        String partnerId = createPartner(token, "Repeat Contact", "repeat@example.com", null);

        mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\"]}".formatted(partnerId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\"]}".formatted(partnerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void recipientCanBeRemoved() throws Exception {
        String token = signUpAndLogin();
        String campaignId = createCampaign(token, "EMAIL");
        String partnerId = createPartner(token, "Removable", "removable@example.com", null);

        String added = mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\"]}".formatted(partnerId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String recipientId = objectMapper.readTree(added).get(0).get("id").asText();

        mockMvc.perform(delete("/campaigns/" + campaignId + "/recipients/" + recipientId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/campaigns/" + campaignId + "/recipients").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void recipientsAreInvisibleToOtherOrganizations() throws Exception {
        String token = signUpAndLogin();
        String campaignId = createCampaign(token, "EMAIL");
        String partnerId = createPartner(token, "Private Contact", "private@example.com", null);
        String added = mockMvc.perform(post("/campaigns/" + campaignId + "/recipients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"partnerIds\": [\"%s\"]}".formatted(partnerId)))
                .andReturn().getResponse().getContentAsString();
        String recipientId = objectMapper.readTree(added).get(0).get("id").asText();

        String outsider = signUpAndLogin();
        mockMvc.perform(get("/campaigns/" + campaignId + "/recipients").header("Authorization", "Bearer " + outsider))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/campaigns/" + campaignId + "/recipients/" + recipientId)
                        .header("Authorization", "Bearer " + outsider))
                .andExpect(status().isNotFound());
    }
}
