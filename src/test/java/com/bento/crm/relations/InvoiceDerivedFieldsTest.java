package com.bento.crm.relations;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * An invoice created from the Finance form (partner, lines, status) must come back complete:
 * sequential number, issue date, sentAt when sent, and a subtotal / VAT / total breakdown.
 */
class InvoiceDerivedFieldsTest extends IntegrationTestBase {

    private String createPartner(String token) throws Exception {
        String response = mockMvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "CUSTOMER", "name": "Invoice Customer", "stage": "CUSTOMER"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void sentInvoiceFromLines_getsNumberDateTimestampsAndVat() throws Exception {
        String token = signUpAndLogin();
        String partnerId = createPartner(token);

        String body = """
                {"type": "CUSTOMER", "partnerId": "%s", "status": "SENT", "total": 20000,
                 "lines": [{"item": "Licence Pro", "qty": 2, "unitPrice": 10000, "type": "service"}]}
                """.formatted(partnerId);

        mockMvc.perform(post("/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber", matchesPattern("FAC-\\d{4}-0001")))
                .andExpect(jsonPath("$.invoiceDate").exists())
                .andExpect(jsonPath("$.sentAt").exists())
                .andExpect(jsonPath("$.subtotal").value(20000.0))
                .andExpect(jsonPath("$.tax").value(4000.0))
                .andExpect(jsonPath("$.total").value(24000.0));

        // Numbers are sequential within the organization.
        mockMvc.perform(post("/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber", matchesPattern("FAC-\\d{4}-0002")));

        // Vendor invoices use their own sequence; an explicit breakdown is kept as sent.
        mockMvc.perform(post("/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "VENDOR", "partnerId": "%s", "status": "DRAFT",
                                 "subtotal": 1000, "tax": 100, "total": 1100}
                                """.formatted(partnerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber", matchesPattern("FRS-\\d{4}-0001")))
                .andExpect(jsonPath("$.sentAt").doesNotExist())
                .andExpect(jsonPath("$.tax").value(100.0))
                .andExpect(jsonPath("$.total").value(1100.0));
    }
}
