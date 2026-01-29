package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BannerModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanManageBannersAndPublicListOnlyShowsEnabled() throws Exception {
        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_banner", "alice123");
        String aliceToken = login("alice_banner", "alice123");

        // Normal user cannot manage banners.
        mockMvc.perform(post("/api/banners")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"b1\",\"imageFileId\":5001,\"sortNo\":10,\"enabled\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Admin creates an enabled banner.
        mockMvc.perform(post("/api/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"b1\",\"imageFileId\":5001,\"sortNo\":10,\"enabled\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Admin creates a disabled banner.
        mockMvc.perform(post("/api/banners")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"b2\",\"imageFileId\":5001,\"sortNo\":9,\"enabled\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Public list should only contain enabled banners.
        JsonNode publicList = listPublicBanners(1, 50);
        Assertions.assertTrue(containsTitle(publicList, "b1"));
        Assertions.assertFalse(containsTitle(publicList, "b2"));

        // Normal user cannot list all banners.
        mockMvc.perform(get("/api/banners")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Admin can list all banners.
        JsonNode adminList = listAllBanners(adminToken, 1, 50);
        Assertions.assertTrue(containsTitle(adminList, "b1"));
        Assertions.assertTrue(containsTitle(adminList, "b2"));
    }

    private boolean containsTitle(JsonNode listResponse, String title) {
        for (JsonNode item : listResponse.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode listPublicBanners(int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/banners/public")
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode listAllBanners(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/banners")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void createUser(String adminToken, String username, String password) throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password
                                + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/token").asText();
    }
}

