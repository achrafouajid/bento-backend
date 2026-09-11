package com.bento.crm.foundation;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Context-load and health smoke checks. Extends {@link IntegrationTestBase} so it shares the
 * singleton Postgres/Redis containers and the migration-built schema rather than declaring its
 * own {@code @Container} fields with no {@code @DynamicPropertySource} to wire them.
 */
class SmokeTest extends IntegrationTestBase {

    @Test
    void testApplicationContextLoads() {
        // Reaching here means the Spring context started against the migrated schema.
    }

    @Test
    void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
