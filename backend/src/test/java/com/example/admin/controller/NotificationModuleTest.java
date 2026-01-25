package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class NotificationModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void announceAndReadFlowWorks() throws Exception {
        String adminToken = login("admin", "admin123");

        // Create a normal user without any role/permission.
        createUser(adminToken, "alice_notif", "alice123");
        String aliceToken = login("alice_notif", "alice123");

        // Normal user cannot announce.
        mockMvc.perform(post("/api/notifications/announce")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"notice-1\",\"content\":\"content-1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Admin announces.
        mockMvc.perform(post("/api/notifications/announce")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"notice-1\",\"content\":\"content-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Alice can see the announcement.
        long notifId = findNotificationIdByTitle(aliceToken, "notice-1");

        // Mark read.
        mockMvc.perform(put("/api/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Verify is_read updated.
        JsonNode listAfter = listNotifications(aliceToken, 1, 50);
        boolean foundRead = false;
        for (JsonNode item : listAfter.at("/data/records")) {
            if (item.at("/id").asLong() == notifId) {
                foundRead = item.at("/isRead").asInt() == 1;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundRead);
    }

    private long findNotificationIdByTitle(String token, String title) throws Exception {
        JsonNode list = listNotifications(token, 1, 50);
        for (JsonNode item : list.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return item.at("/id").asLong();
            }
        }
        throw new AssertionError("notification not found by title: " + title + ", list=" + list.toString());
    }

    private JsonNode listNotifications(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/notifications")
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
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
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
