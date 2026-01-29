package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void incomeTrendShouldAggregateCrowdfundingAndActivityDonations() throws Exception {
        // Test isolation: other module tests may have already inserted donation records into the shared H2 database.
        jdbcTemplate.update("DELETE FROM biz_activity_donation");
        jdbcTemplate.update("DELETE FROM biz_crowdfunding_donation");

        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_stats", "alice123");
        String aliceToken = login("alice_stats", "alice123");

        String activityId = createActivity(
                adminToken,
                "act_stats",
                1,
                LocalDateTime.now().plusDays(1).withNano(0),
                LocalDateTime.now().plusDays(2).withNano(0)
        );

        donateActivity(aliceToken, activityId, 10);
        donateActivity(aliceToken, activityId, 5);

        String projectId = createProject(aliceToken, "cf_stats", LocalDateTime.now().plusDays(2).withNano(0), 100);
        reviewProject(adminToken, projectId, "APPROVE");
        donateCrowdfunding(aliceToken, projectId, 20);

        MvcResult result = mockMvc.perform(get("/api/stats/income-trend")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = json.at("/data");
        Assertions.assertTrue(data.isArray());
        Assertions.assertEquals(7, data.size());

        String today = LocalDate.now().toString();
        double sum = 0;
        Double todayAmount = null;
        for (JsonNode item : data) {
            sum += item.at("/amount").asDouble();
            if (today.equals(item.at("/date").asText())) {
                todayAmount = item.at("/amount").asDouble();
            }
        }
        Assertions.assertEquals(35.0, sum, 0.001);
        Assertions.assertNotNull(todayAmount);
        Assertions.assertEquals(35.0, todayAmount, 0.001);

        // Normal user cannot access stats.
        mockMvc.perform(get("/api/stats/income-trend")
                        .header("Authorization", "Bearer " + aliceToken)
                        .param("days", "7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    private String createActivity(String adminToken, String title, int enabled, LocalDateTime startTime, LocalDateTime endTime)
            throws Exception {
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

    private void donateActivity(String token, String activityId, int amount) throws Exception {
        mockMvc.perform(post("/api/activities/" + activityId + "/donations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":" + amount + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private String createProject(String token, String title, LocalDateTime endTime, int targetAmount) throws Exception {
        String et = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String payload = "{"
                + "\"title\":\"" + title + "\","
                + "\"coverFileId\":5001,"
                + "\"content\":\"<p>hi</p><img src=/api/files/5001/download>\","
                + "\"targetAmount\":" + targetAmount + ","
                + "\"endTime\":\"" + et + "\""
                + "}";

        MvcResult result = mockMvc.perform(post("/api/crowdfunding")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asText();
    }

    private void donateCrowdfunding(String token, String projectId, int amount) throws Exception {
        mockMvc.perform(post("/api/crowdfunding/" + projectId + "/donations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":" + amount + ",\"anonymous\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void reviewProject(String adminToken, String projectId, String action) throws Exception {
        mockMvc.perform(put("/api/crowdfunding/" + projectId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"" + action + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
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
