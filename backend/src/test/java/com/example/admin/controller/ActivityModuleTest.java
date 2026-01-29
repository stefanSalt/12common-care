package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class ActivityModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void activityModuleEndToEnd() throws Exception {
        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_act", "alice123");
        String aliceToken = login("alice_act", "alice123");

        String futureActivityId = createActivity(
                adminToken,
                "a_future",
                1,
                LocalDateTime.now().plusDays(1).withNano(0),
                LocalDateTime.now().plusDays(2).withNano(0)
        );

        createActivity(
                adminToken,
                "a_disabled",
                0,
                LocalDateTime.now().plusDays(1).withNano(0),
                LocalDateTime.now().plusDays(2).withNano(0)
        );

        // Public list should only contain enabled activities.
        JsonNode publicList = listPublicActivities(1, 50);
        Assertions.assertTrue(containsTitle(publicList, "a_future"));
        Assertions.assertFalse(containsTitle(publicList, "a_disabled"));

        // User can sign up.
        mockMvc.perform(post("/api/activities/" + futureActivityId + "/signup")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("SIGNED"));

        // User state is updated.
        mockMvc.perform(get("/api/activities/" + futureActivityId + "/my-state")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.signupStatus").value("SIGNED"))
                .andExpect(jsonPath("$.data.favorited").value(false));

        // Cancel before start is allowed.
        mockMvc.perform(put("/api/activities/" + futureActivityId + "/signup/cancel")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        // Check-in before start should fail (business error).
        mockMvc.perform(put("/api/activities/" + futureActivityId + "/check-in")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // Ongoing activity: sign up and check-in within time window.
        String ongoingActivityId = createActivity(
                adminToken,
                "a_ongoing",
                1,
                LocalDateTime.now().minusHours(1).withNano(0),
                LocalDateTime.now().plusHours(1).withNano(0)
        );

        mockMvc.perform(post("/api/activities/" + ongoingActivityId + "/signup")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("SIGNED"));

        mockMvc.perform(put("/api/activities/" + ongoingActivityId + "/check-in")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("CHECKED_IN"));

        // Cancel after start should fail.
        mockMvc.perform(put("/api/activities/" + ongoingActivityId + "/signup/cancel")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // Donation supports multiple times.
        mockMvc.perform(post("/api/activities/" + futureActivityId + "/donations")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10,\"remark\":\"r1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/activities/" + futureActivityId + "/donations")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5,\"remark\":\"r2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Favorite is idempotent and can be unfavorited.
        mockMvc.perform(post("/api/activities/" + futureActivityId + "/favorite")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/activities/" + futureActivityId + "/favorite")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/activities/" + futureActivityId + "/my-state")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.favorited").value(true));

        mockMvc.perform(delete("/api/activities/" + futureActivityId + "/favorite")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Admin can list signups/donations/favorites; normal user cannot.
        mockMvc.perform(get("/api/activities/signups")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/activities/signups")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/activities/donations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/activities/favorites")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Admin can view stats.
        mockMvc.perform(get("/api/stats/activity-signup-ratio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private boolean containsTitle(JsonNode listResponse, String title) {
        for (JsonNode item : listResponse.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode listPublicActivities(int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/activities/public")
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createActivity(String adminToken, String title, int enabled, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        String st = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String et = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String payload = "{"
                + "\"title\":\"" + title + "\","
                + "\"coverFileId\":5001,"
                + "\"content\":\"<p>hi</p><img src=/api/files/5001/download>\","
                + "\"address\":\"addr\","
                + "\"startTime\":\"" + st + "\","
                + "\"endTime\":\"" + et + "\","
                + "\"signupEnabled\":1,"
                + "\"donateEnabled\":1,"
                + "\"maxParticipants\":2,"
                + "\"donationTarget\":100,"
                + "\"enabled\":" + enabled
                + "}";

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asText();
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

