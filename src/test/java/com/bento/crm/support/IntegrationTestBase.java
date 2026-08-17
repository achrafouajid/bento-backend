package com.bento.crm.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Shared Testcontainers + MockMvc scaffolding for controller integration tests, plus a helper
 * that signs up a fresh organization/admin user and returns a bearer token. Going through the
 * real signup -> login flow (rather than @WithMockUser) exercises the actual JWT/authority wiring,
 * which is what caught bugs like the broken /auth/refresh contract and the Partner response casing
 * mismatch during this audit.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
        "spring.flyway.enabled=false",
        "app.rate-limit.enabled=false",
        "JWT_SECRET=test-only-secret-key-not-for-production-use-minimum-32-bytes"
})
public abstract class IntegrationTestBase {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("crm_test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // Testcontainers assigns random host ports, so the datasource/redis coordinates baked into
    // application.yml (localhost:5432/6379) never match. Without this, Spring silently falls
    // back to those defaults and either hits a developer's local Postgres or fails outright --
    // which is what happened before this fix (see the audit note in AuthControllerTest).
    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected int testSequence = 0;

    @BeforeEach
    void resetSequence() {
        testSequence++;
    }

    /**
     * Signs up a brand-new organization with a fresh admin user (unique email per call so tests
     * don't collide) and returns the admin's access token.
     */
    protected String signUpAndLogin() throws Exception {
        String uniqueSuffix = System.nanoTime() + "-" + testSequence;
        String email = "admin-" + uniqueSuffix + "@example.com";
        String password = "TestPassword123!";

        String signupPayload = """
                {
                  "name": "Test Org %s",
                  "industry": "Technology",
                  "default_currency": "USD",
                  "timezone": "UTC",
                  "admin_email": "%s",
                  "admin_name": "Test Admin",
                  "admin_password": "%s"
                }
                """.formatted(uniqueSuffix, email, password);

        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload))
                .andReturn();

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse).get("access_token").asText();
    }
}
