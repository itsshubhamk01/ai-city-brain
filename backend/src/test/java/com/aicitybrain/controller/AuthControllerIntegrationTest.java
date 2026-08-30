package com.aicitybrain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real Spring context against the real Flyway-seeded H2 database (test
 * profile) — no mocks. This is what proves login, JWT issuance, JWT validation, and
 * role-aware access control actually work end to end, using the exact same demo
 * accounts the app ships with (see V3__seed_users.sql).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_with_seeded_admin_account_succeeds_and_returns_a_jwt() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_with_wrong_password_returns_401_with_no_leaked_detail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"totally-wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void protected_endpoint_without_a_token_returns_401_not_403() throws Exception {
        mockMvc.perform(get("/api/v1/city/status"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protected_endpoint_is_reachable_with_a_valid_token_and_returns_seeded_city_data() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/v1/city/status").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("NovaCity"))
            .andExpect(jsonPath("$.zones", org.hamcrest.Matchers.hasSize(6)));
    }

    @Test
    void citizen_role_cannot_trigger_a_simulation_scenario() throws Exception {
        String token = login("citizen", "citizen123");

        mockMvc.perform(post("/api/v1/simulation/scenario")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarioKey\":\"HEAVY_RAIN\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void admin_role_can_trigger_a_simulation_scenario() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/v1/simulation/scenario")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarioKey\":\"NORMAL\"}"))
            .andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
