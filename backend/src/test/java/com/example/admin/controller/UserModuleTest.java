package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
class UserModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usernameMustBeUniqueAndSoftDeleteHidesUser() throws Exception {
        String adminToken = login("admin", "admin123");

        String username = unique("u");
        long userId = createUser(adminToken, username, "pwd123");

        // Username uniqueness.
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pwd123\",\"nickname\":\"n\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));

        // Soft delete.
        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Deleted user should be invisible to getById.
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));

        // Deleted user should not be in list.
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode item : listJson.at("/data/records")) {
            if (item.at("/id").asLong() == userId) {
                found = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertFalse(found);
    }

    @Test
    void listSupportsRoleFilters() throws Exception {
        String adminToken = login("admin", "admin123");

        MvcResult adminListResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "50")
                        .param("roleCode", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode adminListJson = objectMapper.readTree(adminListResult.getResponse().getContentAsString());
        boolean foundAdmin = false;
        for (JsonNode item : adminListJson.at("/data/records")) {
            if ("admin".equals(item.at("/username").asText())) {
                foundAdmin = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundAdmin);

        MvcResult excludeAdminResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "200")
                        .param("excludeRoleCode", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode excludeAdminJson = objectMapper.readTree(excludeAdminResult.getResponse().getContentAsString());
        boolean hasAdmin = false;
        for (JsonNode item : excludeAdminJson.at("/data/records")) {
            if ("admin".equals(item.at("/username").asText())) {
                hasAdmin = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertFalse(hasAdmin);
    }

    private long createUser(String token, String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asLong();
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

    private String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }
}
