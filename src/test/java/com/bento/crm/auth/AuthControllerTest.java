package com.bento.crm.auth;

import com.bento.crm.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTestBase {

    @Test
    void login_withValidCredentials_returnsTokensAndSnakeCaseUser() throws Exception {
        String uniqueSuffix = System.nanoTime() + "";
        String email = "login-" + uniqueSuffix + "@example.com";
        String password = "TestPassword123!";

        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Login Test Org",
                                  "admin_email": "%s",
                                  "admin_name": "Login Admin",
                                  "admin_password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.display_name").value("Login Admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    /**
     * Two logins for the same user inside one wall-clock second (double-click on "Sign in", a
     * retry, two tabs). The refresh token must differ per login or the second insert hits the
     * unique token-hash constraint and the login 409s.
     */
    @Test
    void login_twiceWithinTheSameSecond_bothSucceed() throws Exception {
        String email = "twice-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Org", "admin_email": "%s", "admin_name": "Admin", "admin_password": "CorrectPassword123!"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        String body = """
                {"email": "%s", "password": "CorrectPassword123!"}
                """.formatted(email);
        String first = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String firstRefresh = objectMapper.readTree(first).get("refresh_token").asText();
        String secondRefresh = objectMapper.readTree(second).get("refresh_token").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(firstRefresh, secondRefresh,
                "each login must mint a distinct refresh token");
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String email = "wrongpass-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Org", "admin_email": "%s", "admin_name": "Admin", "admin_password": "CorrectPassword123!"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "WrongPassword"}
                                """.formatted(email)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void refresh_withValidRefreshToken_issuesNewAccessToken() throws Exception {
        String email = "refresh-" + System.nanoTime() + "@example.com";
        String password = "TestPassword123!";
        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Org", "admin_email": "%s", "admin_name": "Admin", "admin_password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refresh_token").asText();

        // This is the exact request shape AuthApiService.refresh() now sends from the frontend --
        // a JSON body with a snake_case `refresh_token` key, matching RefreshTokenRequest's
        // @JsonProperty. Before that fix the frontend sent an empty body, which this request
        // would reject with 400.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refresh_token": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    void refresh_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_withValidToken_returnsCurrentUserAsJson() throws Exception {
        String token = signUpAndLogin();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.display_name").value("Test Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
